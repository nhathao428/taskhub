-- Lưu ảnh check-in BỊ NGHI VẤN để quản lý đối chiếu bằng mắt (8/2026).
--
-- CHÍNH SÁCH: chỉ lưu khi khuôn mặt không khớp hoặc trượt kiểm tra chống giả mạo.
-- Check-in thành công KHÔNG lưu ảnh — lần hợp lệ vốn không cần bằng chứng, còn lưu tất
-- sẽ tích luỹ hàng nghìn ảnh mặt nhân viên, rủi ro rò rỉ lớn hơn giá trị nghiệp vụ.
-- Dữ liệu sinh trắc học thuộc nhóm nhạy cảm theo Nghị định 13/2023.
--
-- Ảnh mã hoá AES-256-GCM cùng khoá BIOMETRIC_KEY với embedding (xem BiometricCrypto).
-- expires_at: hết hạn thì job FaceCaptureCleanupJob tự xoá (mặc định giữ 30 ngày).
-- ON DELETE CASCADE: xoá bản ghi chấm công là xoá luôn ảnh kèm theo.

CREATE TABLE IF NOT EXISTS attendance_face_captures (
    capture_id      BIGSERIAL PRIMARY KEY,
    attendance_id   BIGINT NOT NULL UNIQUE REFERENCES attendance(attendance_id) ON DELETE CASCADE,
    image_encrypted TEXT NOT NULL,
    reason          VARCHAR(30) NOT NULL,
    captured_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NOT NULL
);

-- Job dọn dẹp quét theo expires_at nên đánh chỉ mục cột này.
CREATE INDEX IF NOT EXISTS idx_face_captures_expires_at ON attendance_face_captures(expires_at);
