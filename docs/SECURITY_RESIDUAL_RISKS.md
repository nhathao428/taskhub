# Residual Security Risks

Tài liệu này liệt kê các risk **đã được xác định và chấp nhận** trong scope đồ án.
Khi triển khai production thực sự, các mục dưới đây cần được nâng lên dạng follow-up.

Cập nhật lần cuối: 2026-05-27 — sau phase security audit + fix.

---

## H2 — JWT lưu trong `localStorage` (XSS exfiltration surface)

**Mức độ hiện tại**: HIGH → **MEDIUM** sau khi áp dụng CSP

**Mitigation đã áp dụng (Security L3)**:
- `Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; ...`
  → Browser block external script + inline script ngoài same-origin → bề mặt XSS giảm mạnh.
- `X-Content-Type-Options: nosniff` → chống MIME-sniff XSS.
- `Referrer-Policy: strict-origin-when-cross-origin` → giảm rò rỉ URL.
- Backend không có endpoint nào render user-input thành HTML (REST/JSON only).
- Frontend React JSX tự động escape user-input; không có `dangerouslySetInnerHTML` trong codebase.
- Flutter `Text` widget escape mặc định.

**Risk còn lại**:
- Nếu mai có code `dangerouslySetInnerHTML` hoặc import library bị compromise → CSP `'unsafe-inline'` cho script vẫn cho phép một số XSS.
- `'unsafe-inline'` script đang cần để Vite dev-mode hoạt động; prod build nên loại bỏ.

**Follow-up khi đưa lên prod thực sự**:
1. Chuyển JWT sang **HttpOnly + Secure + SameSite=Strict cookie**. Mobile vẫn dùng `Authorization: Bearer` (mobile không bị XSS qua cookie).
2. Đổi CSP `script-src` thành `'self'` (bỏ `'unsafe-inline'`) sau khi audit không còn inline script.
3. Implement nonce/hash cho inline script bắt buộc còn lại.
4. Thêm `Subresource Integrity (SRI)` cho asset từ CDN nếu có.

---

## L2 — Không có refresh token mechanism

**Mức độ**: LOW (chấp nhận trong scope đồ án)

**Hiện trạng**:
- Access token JWT hết hạn 2 tiếng. User phải đăng nhập lại sau khi hết hạn.
- Không có refresh token → không thể revoke 1 token cụ thể trước khi hết hạn.

**Mitigation đã áp dụng (Security M7)**:
- `JwtAuthenticationFilter` hit DB qua `loadUserByUsername()` (cached 60s) mỗi request → khi user bị xoá / role đổi, token đang valid sẽ bị vô hiệu trong tối đa 60s.
- Không có blacklist nhưng "user not found" sẽ throw 401 → token đã thuộc về user đã xoá thì bị reject.

**Risk còn lại**:
- Nếu token bị lộ (vd qua XSS trước khi áp CSP, qua copy-paste sai) → kẻ tấn công dùng được trong tối đa 2 giờ.
- Không có "log out from all devices" feature.

**Follow-up khi đưa lên prod thực sự**:
1. Implement refresh token: access token TTL 15 phút + refresh token TTL 7-30 ngày stored ở HttpOnly cookie.
2. Refresh token revocation list ở Redis (key: token jti, value: revoked flag, TTL = remaining lifetime).
3. Endpoint `POST /api/auth/logout` xoá refresh token ở client + thêm jti vào blacklist Redis.
4. Endpoint `POST /api/auth/logout-all` xoá toàn bộ refresh token của user.
5. Có thể bổ sung "device fingerprinting" để detect token reuse từ device khác.

---

## Tham chiếu

- `SecurityConfig.java` — implementation các security header + auth rules.
- `LoginAttemptService.java` — implementation per-account lockout (M4).
- `RateLimitFilter.java` — implementation per-IP rate limit cho auth + AI.
- `AiSuggestionService.java#sanitizePromptInput` — prompt injection mitigation (H3).
- `.specify/memory/constitution.md` v1.1.0 — nguyên tắc gốc.

## Audit history

| Date | Reviewer | Findings | Resolved |
|---|---|---|---|
| 2026-05-27 | `security-auditor` skill | 0 critical / 4 high / 7 medium / 4 low | 13/15 fixed, 2 documented as residual (H2 partial, L2 deferred) |
