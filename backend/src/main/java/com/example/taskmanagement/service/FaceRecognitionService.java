package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.EmployeeFace;
import com.example.taskmanagement.exception.BusinessException;
import com.example.taskmanagement.repository.EmployeeFaceRepository;
import com.example.taskmanagement.security.BiometricCrypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Nhận diện khuôn mặt cho check-in.
 *
 * KIẾN TRÚC: Spring Boot không chạy được PyTorch, nên phần AI nằm ở một service Python
 * riêng (ml/face-recognition/api_service.py). Service đó KHÔNG lưu gì cả — chỉ nhận ảnh
 * và trả về vector 512 chiều. Toàn bộ embedding đã đăng ký do Java giữ trong PostgreSQL
 * và đã mã hoá, để dữ liệu sinh trắc học chỉ nằm ở một nơi duy nhất.
 *
 * Việc so khớp (cosine similarity) làm ở Java chứ không đẩy sang Python, vì so sánh vector
 * là phép tính đơn giản và như vậy embedding không phải rời khỏi backend.
 *
 * Nếu service Python không chạy: mọi lời gọi trả BusinessException với thông báo rõ ràng,
 * KHÔNG làm sập luồng check-in bằng GPS đang chạy bình thường (xem AttendanceService).
 */
@Service
public class FaceRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(FaceRecognitionService.class);

    private final EmployeeFaceRepository faceRepository;
    private final BiometricCrypto crypto;
    private final RestClient restClient;

    @Value("${app.face.service-url:http://127.0.0.1:8000}")
    private String serviceUrl;

    /**
     * Ngưỡng cosine similarity để coi là cùng một người.
     * Với embedding của InceptionResnetV1 (pretrained VGGFace2): cùng người thường 0.7-1.0,
     * khác người thường dưới 0.4-0.5. Đặt 0.65 làm mốc khởi điểm — CHỈNH LẠI sau khi chạy
     * evaluate.py trên dữ liệu thật để cân bằng FAR/FRR (chấm công nên ưu tiên ngưỡng cao
     * để giảm rủi ro chấm công hộ).
     */
    @Value("${app.face.threshold:0.65}")
    private double threshold;

    /** Bắt buộc qua kiểm tra chống giả mạo (chớp mắt) mới cho check-in hay không. */
    @Value("${app.face.require-liveness:true}")
    private boolean requireLiveness;

    public FaceRecognitionService(EmployeeFaceRepository faceRepository, BiometricCrypto crypto) {
        this.faceRepository = faceRepository;
        this.crypto = crypto;
        this.restClient = RestClient.create();
    }

    /** Tính năng chỉ dùng được khi đã cấu hình khoá mã hoá. */
    public boolean isEnabled() {
        return crypto.isConfigured();
    }

    // ------------------------------------------------------------------
    // Đăng ký (enroll)
    // ------------------------------------------------------------------

    /**
     * Đăng ký khuôn mặt cho nhân viên từ nhiều ảnh.
     * Lấy trung bình embedding của các ảnh để ổn định hơn 1 ảnh đơn lẻ (cùng cách
     * enroll.py làm). Ảnh KHÔNG được lưu lại ở bất kỳ đâu.
     */
    @Transactional
    public EmployeeFace enroll(Employee employee, List<String> imagesBase64) {
        requireEnabled();
        if (imagesBase64 == null || imagesBase64.isEmpty()) {
            throw new BusinessException("Cần ít nhất 1 ảnh để đăng ký khuôn mặt");
        }
        if (imagesBase64.size() > 10) {
            throw new BusinessException("Tối đa 10 ảnh mỗi lần đăng ký");
        }

        float[] sum = null;
        int used = 0;
        for (String image : imagesBase64) {
            float[] embedding = extractEmbedding(image);
            if (embedding == null) {
                continue; // ảnh không thấy mặt — bỏ qua, không tính vào trung bình
            }
            if (sum == null) {
                sum = new float[embedding.length];
            } else if (sum.length != embedding.length) {
                throw new BusinessException("Kích thước embedding không đồng nhất giữa các ảnh");
            }
            for (int i = 0; i < embedding.length; i++) {
                sum[i] += embedding[i];
            }
            used++;
        }

        if (used == 0) {
            throw new BusinessException(
                    "Không phát hiện khuôn mặt trong ảnh nào. Chụp lại gần hơn, đủ sáng, nhìn thẳng camera.");
        }
        for (int i = 0; i < sum.length; i++) {
            sum[i] /= used;
        }

        EmployeeFace face = faceRepository.findByEmployee(employee).orElseGet(() -> {
            EmployeeFace created = new EmployeeFace();
            created.setEmployee(employee);
            created.setEnrolledAt(LocalDateTime.now());
            return created;
        });
        face.setEmbeddingEncrypted(crypto.encrypt(sum));
        face.setSampleCount(used);
        face.setUpdatedAt(LocalDateTime.now());

        EmployeeFace saved = faceRepository.save(face);
        log.info("Đã đăng ký khuôn mặt cho employeeId={} từ {}/{} ảnh hợp lệ",
                employee.getEmployeeId(), used, imagesBase64.size());
        return saved;
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(Long employeeId) {
        return faceRepository.existsByEmployeeEmployeeId(employeeId);
    }

    @Transactional
    public void deleteEnrollment(Long employeeId) {
        faceRepository.findByEmployeeEmployeeId(employeeId).ifPresent(face -> {
            faceRepository.delete(face);
            log.info("Đã xoá dữ liệu khuôn mặt của employeeId={}", employeeId);
        });
    }

    // ------------------------------------------------------------------
    // Xác thực (verify)
    // ------------------------------------------------------------------

    /** Kết quả so khớp khuôn mặt cho 1 lần check-in. */
    public record VerifyResult(boolean matched, double similarity, String message) {}

    /**
     * So ảnh vừa chụp với embedding đã đăng ký của CHÍNH nhân viên đó (so khớp 1:1).
     * Dùng cho check-in: người dùng đã đăng nhập nên biết họ tự nhận là ai, chỉ cần xác
     * minh đúng người — không cần quét toàn bộ danh sách.
     */
    @Transactional(readOnly = true)
    public VerifyResult verify(Employee employee, String imageBase64) {
        requireEnabled();
        Optional<EmployeeFace> enrolled = faceRepository.findByEmployee(employee);
        if (enrolled.isEmpty()) {
            throw new BusinessException(
                    "Nhân viên chưa đăng ký khuôn mặt. Vào phần đăng ký khuôn mặt trước khi dùng check-in bằng khuôn mặt.");
        }

        float[] candidate = extractEmbedding(imageBase64);
        if (candidate == null) {
            return new VerifyResult(false, 0.0,
                    "Không phát hiện khuôn mặt trong ảnh. Nhìn thẳng camera, đủ sáng rồi thử lại.");
        }

        float[] reference = crypto.decrypt(enrolled.get().getEmbeddingEncrypted());
        double similarity = cosineSimilarity(candidate, reference);
        boolean matched = similarity >= threshold;
        return new VerifyResult(matched, similarity,
                matched ? "Khuôn mặt khớp" : "Khuôn mặt không khớp với người đã đăng ký");
    }

    /**
     * Kiểm tra chống giả mạo: gửi nhiều frame liên tiếp sang service Python, xem có phát
     * hiện chớp mắt không. Ảnh in ra hoặc ảnh mở trên điện thoại sẽ không chớp mắt được.
     *
     * HẠN CHẾ (nêu rõ trong báo cáo): chưa chặn được video quay sẵn có cảnh chớp mắt.
     */
    public boolean checkLiveness(List<String> framesBase64) {
        requireEnabled();
        if (framesBase64 == null || framesBase64.size() < 3) {
            throw new BusinessException("Cần ít nhất 3 khung hình liên tiếp để kiểm tra chống giả mạo");
        }
        Map<String, Object> body = Map.of("frames_base64", framesBase64);
        Map<?, ?> response = callPythonService("/liveness", body);
        Object live = response.get("live");
        return Boolean.TRUE.equals(live);
    }

    public boolean isLivenessRequired() {
        return requireLiveness;
    }

    public double getThreshold() {
        return threshold;
    }

    // ------------------------------------------------------------------
    // Gọi service Python
    // ------------------------------------------------------------------

    /** Trả về embedding, hoặc null nếu service báo không tìm thấy khuôn mặt trong ảnh. */
    private float[] extractEmbedding(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new BusinessException("Ảnh rỗng");
        }
        Map<?, ?> response = callPythonService("/embed", Map.of("image_base64", imageBase64));

        if (!Boolean.TRUE.equals(response.get("face_detected"))) {
            return null;
        }
        Object raw = response.get("embedding");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            throw new BusinessException("Service nhận diện trả về embedding không hợp lệ");
        }
        float[] embedding = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            if (!(value instanceof Number number)) {
                throw new BusinessException("Service nhận diện trả về embedding không hợp lệ");
            }
            embedding[i] = number.floatValue();
        }
        return embedding;
    }

    private Map<?, ?> callPythonService(String path, Map<String, Object> body) {
        String url = serviceUrl.replaceAll("/+$", "") + path;
        try {
            Map<?, ?> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new BusinessException("Service nhận diện khuôn mặt không trả về dữ liệu");
            }
            return response;
        } catch (RestClientResponseException e) {
            // Service có chạy nhưng từ chối request (vd ảnh hỏng) — thông báo lại cho người dùng.
            log.warn("Service nhận diện trả lỗi {} khi gọi {}", e.getStatusCode().value(), path);
            throw new BusinessException(
                    "Service nhận diện khuôn mặt báo lỗi " + e.getStatusCode().value()
                            + ". Kiểm tra lại ảnh gửi lên.");
        } catch (RestClientException e) {
            // Không kết nối được — thường là quên chạy uvicorn.
            log.error("Không kết nối được service nhận diện tại {}: {}", url, e.getMessage());
            throw new BusinessException(
                    "Không kết nối được service nhận diện khuôn mặt (" + url + "). "
                            + "Chạy: uvicorn api_service:app --port 8000 trong thư mục ml/face-recognition.");
        }
    }

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new BusinessException(
                    "Tính năng nhận diện khuôn mặt chưa bật — thiếu BIOMETRIC_KEY. "
                            + "Tạo khoá bằng: openssl rand -base64 32");
        }
    }

    /** Cosine similarity giữa 2 vector embedding, giá trị trong [-1, 1]. */
    static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new BusinessException("Hai embedding khác kích thước, không so sánh được");
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
