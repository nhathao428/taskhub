# Hướng dẫn Deploy TaskHub (miễn phí)

Tài liệu này hướng dẫn đưa TaskHub lên internet với domain/URL thật, **chỉ dùng các gói free** — phù hợp cho demo, đồ án, hoặc dự án cá nhân lưu lượng thấp.

> Repo hiện **không có** `docker-compose.yml`, `docker-compose.prod.yml` hay `Caddyfile` (đã bị xoá ở một commit trước). Nếu muốn tự host VPS bằng Docker + HTTPS tự động, cần tự viết lại các file này hoặc khôi phục từ lịch sử git (`git log --all --full-history -- Caddyfile`). Muốn deploy lên AWS EC2 xem thêm [`DEPLOY-AWS.md`](./DEPLOY-AWS.md).

## Tổng quan 2 cách

| Cách | Phù hợp khi | Độ khó |
|---|---|---|
| **1. Render Blueprint** (`render.yaml` có sẵn) | Muốn deploy nhanh nhất, backend + frontend + DB trong 1 lần bấm | Dễ nhất |
| **2. Tự deploy từng phần** | Muốn tách backend/frontend ra platform khác nhau, hoặc Render không phù hợp | Trung bình |

Cả 2 cách đều **không tốn tiền** nếu ở đúng free tier, nhưng đọc kỹ phần **giới hạn** bên dưới — free tier luôn có đánh đổi (cold start, DB hết hạn...), không phải "free mãi mãi không giới hạn".

---

## Cách 1 — Render Blueprint (nhanh nhất)

Repo đã có sẵn `render.yaml` mô tả đủ 3 service: backend (Docker), frontend (static), Postgres.

1. Push code lên GitHub (repo đã có sẵn).
2. Vào [render.com](https://render.com) → **New** → **Blueprint** → chọn repo `taskhub`.
3. Render tự đọc `render.yaml` và tạo:
   - `tms-backend` — Spring Boot chạy qua Docker, free tier
   - `tms-frontend` — static site (build từ `frontend/`, free tier)
   - `tms-db` — Postgres free
4. Sau khi tạo xong, vào **tms-backend → Environment**, set thủ công:
   - `GEMINI_API_KEY` — lấy free tại [aistudio.google.com/apikey](https://aistudio.google.com/apikey) (không set thì tính năng gợi ý AI trả lỗi 422, phần còn lại vẫn chạy bình thường)
5. **Đổi mật khẩu demo trước khi public thật**: `render.yaml` đang set sẵn `MANAGER_PASSWORD=Manager@123` và `EMPLOYEE_PASSWORD=Employee@123` ở dạng public (ai đọc code cũng thấy). Nếu không muốn ai cũng đăng nhập được 2 tài khoản này, vào Render dashboard đổi giá trị khác, hoặc sửa `render.yaml` thêm `sync: false` cho 2 key đó rồi set thủ công trên dashboard.
6. Đợi build xong (~5-10 phút lần đầu) → mở URL `tms-frontend` Render cấp (dạng `https://tms-frontend-xxxx.onrender.com`).

### Giới hạn cần biết (free tier Render)

| Giới hạn | Chi tiết |
|---|---|
| Backend cold start | Sleep sau 15 phút không có traffic, request đầu tiên sau đó chờ ~30-60s |
| **Postgres free hết hạn sau 30 ngày** | Render sẽ **xoá luôn database** sau 30 ngày + 14 ngày gia hạn nếu không nâng cấp lên gói trả phí. Đây là giới hạn quan trọng nhất — không phù hợp lưu dữ liệu thật lâu dài nếu không nâng cấp |
| Free Postgres không có backup | Nếu bị xoá là mất hết, không khôi phục được |

**Nếu muốn dữ liệu tồn tại lâu dài mà vẫn free**: đừng dùng Postgres free của Render — dùng **[Neon](https://neon.tech)** (Postgres free, không giới hạn thời gian, không cần thẻ) làm database, rồi trỏ `DB_URL` của backend Render sang Neon thay vì Postgres của Render. Cách làm: xoá phần `databases:` trong `render.yaml` (hoặc bỏ set `fromDatabase`), thay bằng `DB_URL` trỏ tới connection string Neon cấp.

---

## Cách 2 — Tự deploy từng phần

### Bước 1: Build backend thành jar

```bash
cd backend
mvn clean package -DskipTests
# Kết quả: target/taskhub-0.0.1-SNAPSHOT.jar
```

Chạy thử local để kiểm tra jar chạy được (cần set các biến môi trường bắt buộc — xem bảng bên dưới):

```bash
JWT_SECRET=$(openssl rand -base64 48) \
ADMIN_PASSWORD=Admin@12345 \
java -jar target/taskhub-0.0.1-SNAPSHOT.jar
```

Backend cũng có sẵn `backend/Dockerfile` (multi-stage, JDK17 Alpine) nếu platform bạn chọn deploy bằng Docker image thay vì jar trực tiếp.

### Bước 2: Deploy backend (chọn 1)

| Platform | Free tier thật? | Ghi chú |
|---|---|---|
| **Render** (Web Service thủ công, không qua Blueprint) | Có, free vĩnh viễn | Giống Cách 1 nhưng tự cấu hình từng biến thay vì để `render.yaml` làm hết. Cold start sau 15 phút idle |
| **Railway** | Không còn free thật | Chỉ còn $5 credit dùng thử 1 lần, sau đó tính phí theo usage (tối thiểu ~$5/tháng) — **không phù hợp nếu Hào muốn free vĩnh viễn**, chỉ nêu để biết |

→ Với ràng buộc không tốn tiền, **Render Web Service** vẫn là lựa chọn hợp lý nhất cho backend.

Cách deploy trên Render (không dùng Blueprint):
1. New → Web Service → connect repo → Root Directory: `backend`
2. Runtime: Docker (dùng sẵn `backend/Dockerfile`)
3. Set đầy đủ biến môi trường ở bảng dưới
4. Deploy

### Bước 3: Build frontend thành static file

```bash
cd frontend
npm install
npm run build
# Kết quả: dist/ — toàn bộ file HTML/CSS/JS tĩnh
```

### Bước 4: Deploy frontend (chọn 1 — đều free, không cần thẻ)

| Platform | Băng thông free | Ghi chú |
|---|---|---|
| **Cloudflare Pages** | Không giới hạn | Free tier rộng nhất, cho phép dùng thương mại, khuyến nghị nếu không chắc traffic sau này |
| **Netlify** | 100 GB/tháng | Free, cho phép dùng thương mại, tích hợp form/function tiện |
| **Vercel** | 100 GB/tháng | Free (Hobby) — chỉ dành cho dự án cá nhân/phi thương mại, không có build minutes miễn phí cho một số tác vụ |

Cả 3 đều theo flow giống nhau: kết nối repo GitHub → chọn `frontend` làm root directory → build command `npm run build` → output directory `dist` → set biến môi trường `VITE_API_BASE_URL` = URL backend đã deploy ở Bước 2.

### Bước 5: Database — dùng Neon hoặc Supabase (free, không hết hạn)

Vì Postgres free của Render tự xoá sau 30 ngày, nếu deploy thủ công nên dùng luôn:

- **[Neon](https://neon.tech)** — Postgres free, không giới hạn thời gian, không cần thẻ, scale-to-zero khi idle. Khuyến nghị.
- **[Supabase](https://supabase.com)** — Postgres free, không hết hạn nhưng project tự pause sau 7 ngày không hoạt động (tự resume khi có request lại).

Lấy connection string từ 1 trong 2 nơi trên, set vào `DB_URL` của backend.

---

## Biến môi trường cần thiết

Bắt buộc (backend sẽ lỗi/không chạy nếu thiếu):

| Biến | Ý nghĩa |
|---|---|
| `JWT_SECRET` | Khoá ký JWT, ≥32 ký tự ngẫu nhiên. Tạo bằng `openssl rand -base64 48` |
| `ADMIN_PASSWORD` | Mật khẩu tài khoản admin được seed lúc khởi động — thiếu là app throw exception khi start |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Kết nối Postgres (hoặc dùng `DB_HOST`/`DB_PORT`/`DB_NAME` nếu platform cấp riêng từng phần, ví dụ Render) |

Nên set (không bắt buộc nhưng cần cho production thật):

| Biến | Mặc định nếu bỏ trống | Ghi chú |
|---|---|---|
| `GEMINI_API_KEY` | trống — endpoint AI trả 422 | Lấy free tại aistudio.google.com/apikey |
| `GROQ_API_KEY` | trống — không fallback | Free tại console.groq.com/keys. Backend tự động fallback sang Groq khi Gemini trả 429 (hết hạn mức free ~1.500 req/ngày) — Groq free cao hơn nhiều (~14.400 req/ngày). Nên set nếu quy mô user lớn (nhiều manager dùng tính năng gợi ý AI cùng lúc) |
| `SPRING_PROFILES_ACTIVE` | — | Set `postgres` để dùng `application-postgres.properties` (Flyway bật, Hibernate chỉ validate schema thay vì tự sửa) |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Đổi thành domain frontend thật, không đổi sẽ bị CORS chặn |
| `SWAGGER_ENABLED` | `false` | Để `false` ở production — bật lên là lộ toàn bộ API schema |
| `PWD_RESET_EXPOSE_TOKEN` | `false` (đã đúng ở profile postgres) | **Không bật `true` ở production** — bật là link reset password bị trả thẳng trong response, ai cũng chiếm được tài khoản người khác |
| `CACHE_TYPE` | `none` | Set `redis` nếu có Redis server (free tier hiếm platform nào có Redis free lâu dài, để `none` cũng chạy được) |
| `MANAGER_PASSWORD`, `EMPLOYEE_PASSWORD` | trống — không seed | Chỉ set nếu muốn có sẵn tài khoản demo; đổi khỏi giá trị mẫu trong `render.yaml` nếu deploy thật |
| `SEED_SAMPLE_EMPLOYEES` | `false` | `true` để tạo ~30 nhân viên mẫu test tính năng gợi ý AI — chỉ nên bật ở bản demo |

> Xem đầy đủ toàn bộ biến (kèm giải thích chi tiết bằng tiếng Việt) tại [`.env.example`](./.env.example).

---

## Kiểm tra sau khi deploy

```bash
curl https://<backend-url>/api/auth/login -X POST \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"<ADMIN_PASSWORD của bạn>"}'
```

Trả về JWT token → backend chạy đúng. Sau đó mở URL frontend, kiểm tra đăng nhập được và gọi API không bị lỗi CORS.

## Troubleshoot

| Triệu chứng | Nguyên nhân thường gặp |
|---|---|
| Backend crash lúc start | Thiếu `JWT_SECRET` hoặc `ADMIN_PASSWORD` |
| Frontend gọi API bị lỗi CORS | `CORS_ORIGINS` chưa khớp domain frontend thật |
| AI suggestion trả 422 | `GEMINI_API_KEY` chưa set |
| Request đầu tiên chậm 30-60s | Bình thường với free tier có cold start (Render) — nâng cấp gói trả phí mới hết |
| Mất toàn bộ dữ liệu sau ~1 tháng | Dùng Postgres free của Render (tự xoá sau 30 ngày) — chuyển sang Neon/Supabase |
