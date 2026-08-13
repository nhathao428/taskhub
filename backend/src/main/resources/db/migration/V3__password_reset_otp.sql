-- Đổi luồng "quên mật khẩu" từ link (token dài 256-bit) sang mã OTP 6 chữ số gửi qua email.
--
-- token_hash giờ lưu SHA-256 của mã OTP 6 số (10^6 khả năng) thay vì token dài ngẫu nhiên
-- 256-bit -> KHÔNG còn đảm bảo duy nhất giữa các user (2 user có thể trùng OTP ngẫu nhiên
-- cùng lúc), nên phải bỏ ràng buộc UNIQUE trên cột này (nếu không, việc lưu OTP trùng sẽ bị
-- DB từ chối). Verify OTP giờ tra theo (user_id, used=false), không tra trực tiếp token_hash.
--
-- Thêm cột attempts để chống brute-force 6 số: khoá token (used=true) sau N lần nhập sai
-- (xem PasswordResetService.MAX_ATTEMPTS).

ALTER TABLE password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_token_hash_key;
ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS attempts INT NOT NULL DEFAULT 0;
