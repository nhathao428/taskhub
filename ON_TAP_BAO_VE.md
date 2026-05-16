# Tài liệu ôn tập bảo vệ đồ án cơ sở

> Hệ thống Quản lý Công việc cho Doanh nghiệp Nhỏ — tích hợp AI gợi ý nhân viên.
> Đọc kỹ phần 3 (khái niệm) và phần 5 (câu hỏi bảo vệ) là tự tin nhất.

---

## 1. Tóm tắt đề tài (trả lời khi thầy hỏi "đề tài làm gì?")

Hệ thống quản lý công việc cho doanh nghiệp nhỏ, gồm các chức năng: quản lý nhân
viên, dự án, công việc (task), chấm công (có xác thực GPS), và **gợi ý nhân viên
phù hợp cho từng task bằng AI**. Hệ thống có 3 vai trò: ADMIN, MANAGER, EMPLOYEE.

- **Backend:** Spring Boot (REST API, cổng 5000)
- **Frontend web:** React + Vite
- **Mobile:** Flutter
- **CSDL:** PostgreSQL (H2 khi chạy thử local) · **Cache:** Redis
- **AI:** Google Gemini (model `gemini-2.5-flash`)
- Đóng gói bằng **Docker**.

Điểm nổi bật: thay vì dùng công thức tính điểm cố định, hệ thống để **LLM (Gemini)
đánh giá định tính** và xếp hạng nhân viên, kèm lý do bằng tiếng Việt.

---

## 2. Công nghệ sử dụng & LÝ DO chọn (thầy rất hay hỏi "tại sao dùng X?")

| Công nghệ | Vai trò | Lý do chọn |
|---|---|---|
| **Spring Boot** | Framework backend | Chuẩn công nghiệp cho REST API Java, cấu hình nhanh, hệ sinh thái lớn (Security, Data JPA) |
| **REST API** | Giao tiếp client–server | Đơn giản, dùng HTTP/JSON, tách rời frontend và backend |
| **JWT** | Xác thực | Stateless — server không cần lưu phiên, dễ mở rộng nhiều máy chủ |
| **Spring Data JPA / Hibernate** | ORM | Không phải viết SQL thủ công cho CRUD, ánh xạ object ↔ bảng |
| **PostgreSQL** | CSDL quan hệ | Mạnh, miễn phí, hỗ trợ ràng buộc toàn vẹn dữ liệu tốt |
| **Redis** | Cache | Giảm tải DB và giảm số lần gọi AI (lưu kết quả 5 phút) |
| **React + Vite** | Web frontend | SPA mượt, Vite build nhanh |
| **Flutter** | Mobile | Một mã nguồn chạy cả Android lẫn iOS |
| **Google Gemini** | AI gợi ý | Có gói miễn phí, hiểu tiếng Việt tốt, giải thích được quyết định |
| **Docker** | Đóng gói | Chạy giống nhau ở mọi máy, không lo "máy tôi chạy được" |

---

## 3. Khái niệm cốt lõi — giải thích ngắn gọn dễ nhớ

**REST API:** Kiểu thiết kế giao tiếp qua HTTP. Mỗi tài nguyên (nhân viên, task...)
có URL riêng, dùng các method GET (đọc), POST (tạo), PUT/PATCH (sửa), DELETE (xóa).
Dữ liệu trao đổi dạng JSON.

**JWT (JSON Web Token):** Một chuỗi token server cấp cho client sau khi đăng nhập.
Gồm 3 phần (Header.Payload.Signature). Client gắn token vào header
`Authorization: Bearer <token>` cho mỗi request. Server chỉ cần **kiểm tra chữ ký**
là biết token hợp lệ — **không cần lưu gì** (stateless). Token hết hạn sau 24h.

**Stateless vs Session:** Session thì server phải lưu trạng thái đăng nhập của
từng người (tốn bộ nhớ, khó mở rộng nhiều server). JWT thì mọi thông tin nằm trong
token, server không lưu → dễ mở rộng.

**BCrypt:** Thuật toán băm mật khẩu. Đặc điểm: (1) **chậm có chủ đích** → kẻ tấn
công khó dò; (2) **tự sinh salt** (chuỗi ngẫu nhiên thêm vào mỗi mật khẩu) → 2 người
cùng mật khẩu vẫn cho hash khác nhau, chống tấn công "rainbow table". Không dùng
MD5/SHA-1 vì chúng quá nhanh và không có salt.

**ORM / JPA / Hibernate:** ORM = ánh xạ đối tượng ↔ bảng CSDL. JPA là *đặc tả*
(chuẩn), Hibernate là *bản hiện thực* phổ biến nhất. Lập trình viên thao tác với
object Java, Hibernate tự sinh SQL.

**Lazy loading & open-in-view:** Quan hệ giữa các bảng (vd Task → Project) mặc định
nạp "lười" (lazy) — chỉ tải khi cần. `open-in-view=true` giữ phiên Hibernate mở
đến khi trả response, để serialize được dữ liệu liên kết mà không bị lỗi "no session".

**Spring Security & phân quyền:** Bảo vệ API theo 2 lớp — `SecurityConfig` quy định
URL nào cần vai trò gì; `@PreAuthorize` kiểm soát ở mức từng hàm. 3 vai trò:
ADMIN > MANAGER > EMPLOYEE (cấp trên có mọi quyền của cấp dưới).

**Cache (Redis):** Lưu tạm kết quả hay dùng để lần sau lấy ra ngay, không phải
tính lại / gọi lại. Kết quả gợi ý AI được cache 5 phút theo nội dung task.

**SPA (Single Page Application):** Web tải 1 lần, sau đó chuyển trang bằng
JavaScript không tải lại toàn trang → mượt như app. React + React Router làm việc này.

**LLM (Large Language Model):** Mô hình ngôn ngữ lớn (như Gemini) huấn luyện trên
khối văn bản khổng lồ, hiểu và sinh văn bản tự nhiên.

**Rate limiting:** Giới hạn số request mỗi IP trong một khoảng thời gian, chống
spam / dò mật khẩu (brute force). Ở đây: tối đa 20 lần/phút cho đăng nhập, 10 lần/phút
cho gọi AI; vượt thì trả lỗi HTTP 429.

---

## 4. Luồng hoạt động chính

**Luồng đăng nhập:**
1. Client gửi email + mật khẩu → `POST /api/auth/login`.
2. Backend tìm user, so khớp mật khẩu bằng BCrypt.
3. Đúng → sinh JWT (chứa username + vai trò), trả về client.
4. Các request sau gắn `Authorization: Bearer <token>`; một filter kiểm tra token
   rồi nạp thông tin user vào SecurityContext.

**Luồng AI gợi ý nhân viên (quan trọng nhất — nên thuộc):**
1. Quản lý nhập tiêu đề + mô tả công việc (+ kỹ năng yêu cầu nếu có).
2. Backend kiểm tra cache — nếu có kết quả cũ thì trả ngay.
3. Nếu chưa có: backend **gom dữ liệu thô** của mọi nhân viên — lịch sử task
   (đã hoàn thành / đang làm), mức độ đúng hạn, số ngày chấm công 30 ngày gần nhất,
   kỹ năng, chức danh, phòng ban.
4. Backend **xây prompt tiếng Việt** chứa task + dữ liệu nhân viên, gửi cho Gemini.
5. Gemini **tự xếp hạng** TOP 5 nhân viên, trả về JSON `[{employeeId, rank, reasoning}]`.
6. Backend parse JSON, lưu cache 5 phút, trả cho client hiển thị.

> Ý quan trọng: **backend KHÔNG tự tính điểm**. Nó chỉ gom dữ liệu thô; chính LLM
> đánh giá định tính và giải thích.

---

## 5. BỘ CÂU HỎI BẢO VỆ THƯỜNG GẶP + trả lời mẫu

**Q1. Tại sao dùng JWT mà không dùng session?**
→ JWT là stateless: server không phải lưu phiên của từng người, nhẹ và dễ mở rộng
ra nhiều máy chủ. Session thì server phải lưu trạng thái, tốn bộ nhớ và khó mở rộng.

**Q2. Token bị lộ thì sao? Lưu token ở đâu?**
→ Token lưu ở phía client (localStorage). Token có hạn 24h nên rủi ro giới hạn theo
thời gian. Production bắt buộc dùng HTTPS để token không bị nghe lén trên đường truyền.

**Q3. BCrypt là gì? Salt để làm gì?**
→ BCrypt là hàm băm mật khẩu, chậm có chủ đích để chống dò. Salt là chuỗi ngẫu nhiên
thêm vào mỗi mật khẩu trước khi băm → hai người cùng mật khẩu vẫn ra hash khác nhau,
chống tấn công bằng bảng tra sẵn (rainbow table).

**Q4. Tính năng AI gợi ý hoạt động thế nào?**
→ (Trả lời theo luồng ở phần 4) Backend gom dữ liệu thô của nhân viên, xây prompt
tiếng Việt, gửi cho Gemini; Gemini xếp hạng top 5 kèm lý do; backend trả về và cache 5 phút.

**Q5. Tại sao dùng AI/LLM mà không tự viết công thức tính điểm?**
→ Công thức trọng số cố định cứng nhắc, khó xử lý dữ liệu tự do (kỹ năng nhập bằng
văn bản, mô tả task chung chung). LLM đánh giá linh hoạt, suy luận được từ mô tả, và
**giải thích được lý do** cho người quản lý — điều công thức số không làm được.

**Q6. Nếu Gemini lỗi, mất mạng hoặc hết hạn mức thì sao?**
→ Hệ thống bắt lỗi: chưa cấu hình API key → trả HTTP 422 kèm thông báo rõ; Gemini
quá tải (503) hoặc hết hạn mức (429) → trả thông báo "thử lại sau" thay vì lỗi khó hiểu.
Tính năng AI là phụ trợ, các chức năng khác vẫn chạy bình thường.

**Q7. Phân quyền hoạt động thế nào?**
→ 3 vai trò ADMIN/MANAGER/EMPLOYEE. Spring Security kiểm 2 lớp: `SecurityConfig`
quy định URL nào cho vai trò nào; `@PreAuthorize` kiểm ở mức hàm. Ví dụ nhân viên
chỉ xem được dự án, chỉ MANAGER/ADMIN mới thêm/sửa/xóa.

**Q8. ORM (JPA/Hibernate) là gì, lợi ích?**
→ ORM ánh xạ đối tượng Java với bảng CSDL. Lợi ích: không phải viết SQL thủ công
cho CRUD, code ngắn gọn, ít lỗi, dễ đổi loại CSDL.

**Q9. Redis / cache để làm gì?**
→ Lưu tạm kết quả hay dùng. Kết quả gợi ý AI cache 5 phút: gọi lại cùng task lấy ngay
từ cache (~vài ms thay vì ~1–2 giây), vừa nhanh vừa đỡ tốn hạn mức gọi Gemini.

**Q10. Rate limiting để làm gì?**
→ Giới hạn số request mỗi IP để chống spam và tấn công dò mật khẩu; đồng thời bảo vệ
hạn mức gọi Gemini. Vượt ngưỡng → trả HTTP 429.

**Q11. Hệ thống có mấy bảng/thực thể?**
→ 7 thực thể chính: User, Employee, Project, Task, Attendance, OfficeLocation,
Suggestion. (Nắm sơ quan hệ: User 1–1 Employee; Employee được gán nhiều Task;
Project có nhiều Task; Employee có nhiều bản ghi Attendance.)

**Q12. Frontend và backend tách rời thế nào?**
→ Backend chỉ cung cấp REST API (JSON). Frontend (React) là ứng dụng riêng, gọi API
qua HTTP. Lợi ích: phát triển song song, một backend phục vụ cả web lẫn mobile.

**Q13. Docker để làm gì?**
→ Đóng gói ứng dụng + môi trường thành container, chạy giống nhau ở mọi máy, tránh
lỗi "máy tôi chạy được, máy khác không". `docker-compose` khởi động cả backend +
PostgreSQL + Redis bằng một lệnh.

**Q14. Chấm công GPS xác thực thế nào?**
→ Khi nhân viên check-in, hệ thống lấy tọa độ GPS, tính khoảng cách tới văn phòng
bằng công thức Haversine; nếu nằm trong bán kính cho phép mới hợp lệ.

**Q15. Điểm yếu của hệ thống? (xem phần 6)**

**Q16. Nếu có 10.000 nhân viên thì sao?**
→ Prompt gửi cho AI sẽ rất dài → chậm và tốn token. Hướng giải quyết: lọc sơ bộ
ứng viên trước (theo phòng ban/kỹ năng) rồi mới gửi nhóm nhỏ cho AI; hoặc dùng
vector embedding để tìm kiếm ngữ nghĩa.

---

## 6. Điểm mạnh — Điểm yếu — Hướng phát triển

**Điểm mạnh:** kiến trúc 3 tầng rõ ràng; bảo mật nhiều lớp (JWT, BCrypt, phân quyền,
rate limiting); tích hợp AI thực tế và giải thích được; chạy đa nền tảng (web + mobile).

**Điểm yếu (thẳng thắn nhận khi thầy hỏi):**
- Phụ thuộc dịch vụ AI bên ngoài — mất mạng/hết hạn mức thì tính năng gợi ý ngừng.
- Prompt dài thêm khi số nhân viên tăng → chậm, tốn token.
- Cache để trong bộ nhớ/Redis một máy — chạy nhiều máy chủ cần cấu hình thêm.
- Phân quyền mới ở mức vai trò, chưa chi tiết tới từng bản ghi.

**Hướng phát triển:** lọc ứng viên trước khi gửi AI; thêm mô hình AI dự phòng
chạy nội bộ; thông báo realtime; báo cáo/thống kê nâng cao.

---

## 7. Mẹo khi bảo vệ

- Nắm chắc **luồng AI gợi ý** (phần 4) — đây là điểm nhấn, thầy chắc chắn hỏi.
- Khi hỏi "tại sao dùng X" → luôn trả lời theo cặp **"vấn đề → X giải quyết ra sao"**.
- Không biết câu nào thì thành thật, đừng chế; nói hướng mình sẽ tìm hiểu.
- Thuộc con số: 3 vai trò, 7 thực thể, cổng 5000, token 24h, cache 5 phút.
- Mở sẵn code các file chính: `AiSuggestionService.java`, `SecurityConfig.java`,
  `RateLimitFilter.java` để minh họa khi cần.
