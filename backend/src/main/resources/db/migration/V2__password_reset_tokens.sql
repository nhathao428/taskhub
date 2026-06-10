-- Luồng "quên mật khẩu" — bảng lưu token đặt lại mật khẩu.
-- Chỉ lưu SHA-256 hash của token (64 hex chars), KHÔNG lưu token thật:
-- DB rò rỉ cũng không tái tạo được link đặt lại hợp lệ.

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id    BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tra cứu khi xác thực token (lookup theo hash) và khi dọn token cũ của 1 user.
CREATE INDEX IF NOT EXISTS idx_prt_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_prt_user_id    ON password_reset_tokens (user_id);
