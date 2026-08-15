package com.example.taskmanagement.security;

import com.example.taskmanagement.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Mã hoá / giải mã dữ liệu sinh trắc học (embedding khuôn mặt) trước khi ghi xuống DB.
 *
 * VÌ SAO CẦN: embedding khuôn mặt là dữ liệu sinh trắc học — không đổi được như mật khẩu.
 * Nếu DB bị rò rỉ mà lưu embedding dạng thô, kẻ tấn công có thể dùng nó để dựng lại đặc
 * trưng khuôn mặt hoặc tấn công hệ thống khác dùng cùng loại model. Khác với mật khẩu,
 * KHÔNG thể hash một chiều được vì lúc xác thực cần so khoảng cách giữa 2 vector, nên
 * bắt buộc phải mã hoá 2 chiều (giải mã được) thay vì hash.
 *
 * Thuật toán: AES-256-GCM (vừa mã hoá vừa chống sửa đổi). Mỗi lần mã hoá sinh IV ngẫu
 * nhiên 12 byte riêng, ghép vào trước ciphertext rồi encode base64 để lưu cột TEXT.
 *
 * Khoá lấy từ biến môi trường BIOMETRIC_KEY (32 byte, encode base64). Tạo khoá mới:
 *     openssl rand -base64 32
 * KHÔNG hardcode khoá vào code hay commit lên git. Mất khoá = mất toàn bộ embedding đã
 * đăng ký (phải cho nhân viên đăng ký lại khuôn mặt), nên cần sao lưu khoá riêng.
 */
@Component
public class BiometricCrypto {

    private static final Logger log = LoggerFactory.getLogger(BiometricCrypto.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;      // khuyến nghị chuẩn cho GCM
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;     // AES-256

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.biometric.key:}")
    private String base64Key;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        if (base64Key == null || base64Key.isBlank()) {
            log.warn("BIOMETRIC_KEY chưa cấu hình — tính năng đăng ký/xác thực khuôn mặt sẽ bị từ chối. "
                    + "Tạo khoá bằng: openssl rand -base64 32");
            return;
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("BIOMETRIC_KEY không phải chuỗi base64 hợp lệ", e);
        }
        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "BIOMETRIC_KEY phải là 32 byte sau khi decode base64 (AES-256), đang là " + keyBytes.length);
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        log.info("BiometricCrypto đã sẵn sàng (AES-256-GCM).");
    }

    /** Có cấu hình khoá hay chưa — service gọi trước để trả lỗi rõ ràng thay vì crash. */
    public boolean isConfigured() {
        return secretKey != null;
    }

    /**
     * Mã hoá vector embedding thành chuỗi base64 để lưu DB.
     * Định dạng sau khi decode base64: [IV 12 byte][ciphertext + tag].
     */
    public String encrypt(float[] embedding) {
        return encryptBytes(floatsToBytes(embedding));
    }

    /** Giải mã chuỗi base64 trong DB về vector embedding. */
    public float[] decrypt(String base64Payload) {
        return bytesToFloats(decryptBytes(base64Payload));
    }

    /**
     * Mã hoá dữ liệu nhị phân bất kỳ (dùng cho ảnh khuôn mặt lúc check-in bị nghi vấn).
     * Cùng định dạng với encrypt(float[]): [IV 12 byte][ciphertext + tag], encode base64.
     */
    public String encryptBytes(byte[] plaintext) {
        requireConfigured();
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            // Không log nội dung embedding — đây là dữ liệu sinh trắc học.
            throw new BusinessException("Không mã hoá được dữ liệu khuôn mặt: " + e.getMessage());
        }
    }

    /** Giải mã dữ liệu nhị phân đã mã hoá bằng encryptBytes(). */
    public byte[] decryptBytes(String base64Payload) {
        requireConfigured();
        try {
            byte[] combined = Base64.getDecoder().decode(base64Payload);
            if (combined.length <= IV_LENGTH_BYTES) {
                throw new BusinessException("Dữ liệu khuôn mặt đã lưu bị hỏng (quá ngắn)");
            }
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(combined, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    "Không giải mã được dữ liệu khuôn mặt (sai BIOMETRIC_KEY hoặc dữ liệu bị sửa đổi)");
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new BusinessException(
                    "Chưa cấu hình BIOMETRIC_KEY — không thể xử lý dữ liệu khuôn mặt. "
                            + "Tạo khoá bằng: openssl rand -base64 32");
        }
    }

    private static byte[] floatsToBytes(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * Float.BYTES);
        for (float v : values) {
            buffer.putFloat(v);
        }
        return buffer.array();
    }

    private static float[] bytesToFloats(byte[] bytes) {
        if (bytes.length % Float.BYTES != 0) {
            throw new BusinessException("Dữ liệu khuôn mặt đã lưu bị hỏng (độ dài không hợp lệ)");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] values = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }
}
