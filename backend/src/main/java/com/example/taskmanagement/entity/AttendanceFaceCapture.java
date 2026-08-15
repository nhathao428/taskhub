package com.example.taskmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ảnh chụp lúc check-in, LƯU CÓ ĐIỀU KIỆN để quản lý đối chiếu bằng mắt.
 *
 * CHÍNH SÁCH LƯU TRỮ (quan trọng — nêu rõ trong báo cáo đồ án):
 *   - CHỈ lưu khi lần check-in đó bị nghi vấn: khuôn mặt không khớp, hoặc trượt kiểm tra
 *     chống giả mạo. Check-in thành công KHÔNG lưu ảnh.
 *   - Ảnh được mã hoá AES-256-GCM cùng khoá với embedding (xem BiometricCrypto).
 *   - Có hạn lưu (mặc định 30 ngày), hết hạn sẽ bị job dọn dẹp tự động xoá.
 *
 * Vì sao không lưu mọi lần check-in: dữ liệu sinh trắc học thuộc nhóm nhạy cảm (Nghị định
 * 13/2023). Lưu toàn bộ sẽ tích luỹ hàng nghìn ảnh mặt nhân viên — rủi ro nếu DB rò rỉ lớn
 * hơn nhiều so với giá trị nghiệp vụ, vì lần chấm công hợp lệ vốn không cần bằng chứng.
 * Chỉ lần bị nghi mới cần ảnh để con người phán đoán đúng/sai.
 */
@Entity
@Table(name = "attendance_face_captures")
@Getter
@Setter
@ToString(exclude = {"attendance", "imageEncrypted"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AttendanceFaceCapture implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "capture_id")
    private Long captureId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false, unique = true)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Attendance attendance;

    /**
     * Ảnh JPEG/PNG đã mã hoá, base64 của [IV][ciphertext+tag].
     * @JsonIgnore: chỉ trả ra qua endpoint riêng có kiểm tra quyền ADMIN/MANAGER,
     * tuyệt đối không lọt vào response của các API attendance thông thường.
     */
    @JsonIgnore
    @Column(name = "image_encrypted", nullable = false, columnDefinition = "TEXT")
    private String imageEncrypted;

    /** Lý do lưu lại: FACE_MISMATCH hoặc LIVENESS_FAILED. */
    @Column(name = "reason", nullable = false, length = 30)
    private String reason;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt = LocalDateTime.now();

    /** Quá thời điểm này thì job dọn dẹp sẽ xoá bản ghi. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
