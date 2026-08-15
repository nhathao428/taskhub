-- Đăng ký khuôn mặt cho check-in bằng nhận diện (đồ án chuyên ngành 8/2026).
--
-- KHÔNG lưu ảnh. Chỉ lưu embedding 512 chiều do model FaceNet sinh ra, đã mã hoá
-- AES-256-GCM bằng khoá từ biến môi trường BIOMETRIC_KEY (xem BiometricCrypto.java).
-- Ảnh gốc bị huỷ ngay sau khi trích xuất embedding — không ghi đĩa, không ghi DB.
--
-- Vì sao mã hoá 2 chiều thay vì hash 1 chiều như mật khẩu: xác thực khuôn mặt phải so
-- khoảng cách cosine giữa 2 vector, nên bắt buộc đọc lại được giá trị gốc.
--
-- Mỗi nhân viên tối đa 1 bản ghi (UNIQUE trên employee_id) — đăng ký lại thì ghi đè.
-- ON DELETE CASCADE: xoá nhân viên là xoá luôn dữ liệu sinh trắc học của họ.

CREATE TABLE IF NOT EXISTS employee_faces (
    employee_face_id    BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL UNIQUE REFERENCES employees(employee_id) ON DELETE CASCADE,
    embedding_encrypted TEXT NOT NULL,
    sample_count        INT NOT NULL DEFAULT 1,
    enrolled_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);

-- Bảng attendance: ghi lại kết quả xác thực khuôn mặt của từng lần check-in.
-- Chỉ lưu true/false + độ tương đồng, KHÔNG lưu ảnh hay embedding của lần check-in đó.
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS face_verified BOOLEAN;
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS face_similarity REAL;
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS liveness_passed BOOLEAN;
