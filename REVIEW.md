# Code Review — TaskHub

> Adversarial review theo phương pháp **GSD** (`gsd-build/get-shit-done` → agent `gsd-code-reviewer`, depth = standard).
> Stance: giả định code có lỗi, chỉ ghi finding **chứng minh được**. Phân loại: **BLOCKER** (sai hành vi / bảo mật / mất dữ liệu, phải sửa trước khi ship) · **WARNING** (giảm chất lượng/độ bền, nên sửa).
> Ngày: 2026-06-08 · Phạm vi: backend (Java/Spring), frontend (React), mobile (Flutter).

## Tổng quan
Backend được làm khá chắc về bảo mật (BCrypt-12, JWT, rate-limit + lockout, CSP/HSTS, anti-enumeration, exception handler không leak nội bộ). Quick-scan (secrets/eval/innerHTML/debug/empty-catch) **sạch**. Không có BLOCKER nào ở mức "sai hành vi nghiêm trọng / lỗ hổng". Các finding dưới đây là độ bền & tính đúng đắn ở rìa.

---

## WARNING

### W1 — Endpoint `/me` giả định luôn có hồ sơ Employee (backend)
`SecurityConfig` cho EMPLOYEE/MANAGER/ADMIN gọi `GET /api/employees/me`, `/api/tasks/me`, `/api/attendance/me`, `POST /api/attendance/me/checkin|checkout`. Nhưng `CurrentUserService.getCurrentEmployee()` ném `ResourceNotFoundException` (404) khi user **chưa link Employee**.
- Sau fix auto-provision, **chỉ user tự đăng ký** mới có Employee. Tài khoản **ADMIN/MANAGER seed** (và user bị xoá Employee) vẫn 404 khi tự check-in / xem việc của mình.
- **Tác động:** manager/admin bấm check-in trên mobile → lỗi 404 khó hiểu.
- **Đề xuất:** hoặc backfill Employee cho mọi user thiếu (ApplicationRunner), hoặc trả thông báo rõ "Tài khoản chưa gắn hồ sơ nhân viên" thay vì 404 generic, hoặc bỏ MANAGER/ADMIN khỏi matcher `/attendance/me/**` nếu họ không phải nhân viên chấm công.

### W2 — Mobile nuốt lỗi ở các thao tác ghi (silent failure)
`mobile/lib/providers/data_provider.dart`: `addEmployee`, `addProject`, `addTask`, `updateTask`, `updateMyTaskStatus`, `checkIn`, `checkOut` đều `catch (e) { return null; }` — **mất hẳn message lỗi** từ server. UI chỉ hiện "thất bại" chung chung, không phân biệt 403 / 422 / mạng.
- Trái với rule `coding-style` (không nuốt lỗi im lặng) và với cách các `fetch*` đã làm đúng (set `_error`).
- **Đề xuất:** set một field `_error` (hoặc rethrow `ApiException`) để màn hình hiện message thật như `_unwrap` đã bóc.

### W3 — Tính khoảng cách geofence trên client bằng chuỗi Taylor tự chế (mobile)
`attendance_screen.dart` tự cài `_cos/_sqrt/_asin` bằng Taylor 3 số hạng để tính haversine. Các chuỗi này **chỉ đúng khi góc nhỏ**; `_cos` phân kỳ khi x > ~1.5 rad, `_asin` hội tụ kém gần 1. Với văn phòng ở xa hoặc vĩ độ lớn, khoảng cách/"trong vùng" hiển thị có thể sai.
- **Giảm nhẹ:** backend `GeofenceService` mới là nguồn quyết định (client chỉ gợi ý), nên không sai nghiệp vụ — chỉ sai **hint hiển thị**.
- **Đề xuất:** dùng `dart:math` (`sin/cos/asin/sqrt`) cho đúng, gọn hơn và hết phân kỳ.

### W4 — JWT mang role nhưng không thu hồi được sớm
Sau khi nhúng `role` vào JWT, nếu ADMIN hạ quyền một MANAGER, token cũ vẫn khai `role=MANAGER` tới khi **hết hạn** (không có refresh/blacklist; cache `user_details` 60s chỉ ảnh hưởng authorization phía server, không ảnh hưởng claim client đọc).
- **Tác động:** UI client có thể hiện nhầm menu quản lý tới khi token hết hạn; **authorization thật ở server vẫn đúng** (dựa SecurityContext), nên không phải lỗ hổng truy cập.
- **Đề xuất:** giảm `app.jwt.expiration` hoặc thêm refresh-token + danh sách thu hồi (đã nằm ở "Hướng phát triển" — ghi nhận là nợ kỹ thuật).

### W5 — `console.error` trong code production (frontend)
`frontend/src/pages/Dashboard.jsx:115` còn `console.error('Lỗi khi tải dữ liệu dashboard:', err)`. Trái rule "no debug statements". (`ErrorBoundary.jsx` log lỗi là chấp nhận được.)
- **Đề xuất:** bỏ hoặc thay bằng cơ chế báo lỗi UI.

### W6 — Auto-provision Employee để `lastName = ""` (backend)
`UserService.register` tạo Employee với `firstName = username`, `lastName = ""`. Hiển thị `employeeName` sẽ dư khoảng trắng đuôi; gom nhóm theo họ ra rỗng.
- **Đề xuất:** để `lastName = null` (cột nullable) hoặc tách username thô, và để Manager bổ sung sau.

---

## Đã kiểm tra — KHÔNG phải lỗi
- `UpdateTaskStatusRequest` có `@NotBlank @Pattern` → `updateMyTaskStatus` không NPE vì status null/invalid.
- `TaskService.updateTask` guard `if (request.status() != null)` trước `toLowerCase()` → an toàn.
- Không có secret hardcode; `ADMIN_PASSWORD`/`MANAGER_PASSWORD` đọc từ env, fail-fast khi thiếu.
- Không dùng `eval`/`innerHTML`/`dangerouslySetInnerHTML`; query JPA tham số hoá (không nối chuỗi SQL).

---

## Trạng thái xử lý (2026-06-08)
- ✅ **W2 — ĐÃ SỬA.** `data_provider.dart`: 7 thao tác ghi giờ set `_error` + `notifyListeners()` (hết nuốt im lặng); `tasks_screen`/`projects_screen` hiện message thật từ server thay vì "thất bại" chung chung.
- ✅ **W5 — ĐÃ SỬA.** Bỏ `console.error` ở `Dashboard.jsx` (catch có comment, không phải empty-catch).
- ✅ **W6 — ĐÃ SỬA.** `UserService.register` tách họ/tên từ username (1 từ → lastName=""); trim chỗ ghép tên (mobile `fullName`, prompt AI). *Không* nới `last_name` thành nullable vì sẽ làm mobile crash (`as String` ép null) — tránh "fix tạo bug rộng hơn".
- ⏳ **W1** (backfill / message rõ cho `/me`) — chưa làm; nên backfill `Employee` cho user thiếu (giải quyết luôn tài khoản employee cũ).
- ⏳ **W3** (geofence Taylor tự chế) — chưa làm; thay bằng `dart:math`.
- ⏳ **W4** (JWT thu hồi sớm) — nợ kỹ thuật, để đồ án chuyên ngành.

Verify sau sửa: backend `UserServiceTest` 6/6 PASS · mobile `flutter analyze` sạch · frontend build OK.
