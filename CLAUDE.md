# Task Management System — AI Context

## Stack nhanh
| Tầng | Công nghệ | Phiên bản |
|---|---|---|
| Backend | Java, Spring Boot, Maven | Java 17+, Spring Boot 3.5.0 |
| Auth | Spring Security + JWT (jjwt) | 0.12.x |
| ORM | Spring Data JPA / Hibernate | - |
| Frontend | React, Vite, Tailwind CSS | React 18, Vite 5 |
| HTTP Client | Axios + JWT interceptor | - |
| Charts | Chart.js + react-chartjs-2 | - |
| Routing | React Router DOM | v6 |
| Mobile | Flutter, Dart | Flutter 3.x, Dart ≥ 3.0 |
| Database | PostgreSQL | 16 |
| Cache | Redis + Spring Cache | 7 |
| Container | Docker, Docker Compose | - |
| AI | OpenAI GPT API | gpt-4o-mini |

## Cấu trúc thư mục
```
/backend    → Spring Boot app (Maven, src/main/java/...)
/frontend   → React + Vite + Tailwind
/mobile     → Flutter app
/docs       → Tài liệu
docker-compose.yml → PostgreSQL 16 + Redis 7 + services
.env.example       → Biến môi trường mẫu
```

## Backend conventions
- Package gốc: xem `backend/src/main/java/`
- Global exception: `@RestControllerAdvice` đã có sẵn
- API response: dùng `ApiResponse<T>` wrapper (đã có)
- Auth flow: JWT filter → SecurityContext → `@PreAuthorize`
- JWT lib: jjwt 0.12.x → dùng `Jwts.builder()`, KHÔNG dùng deprecated API cũ
- Test: Mockito + JUnit 5, KHÔNG dùng PowerMock
- DB migration: kiểm tra có Flyway/Liquibase chưa trước khi tạo schema

## Frontend conventions
- Tailwind utility classes, KHÔNG viết CSS file riêng trừ khi cần
- Axios instance đã có JWT interceptor — import từ file config sẵn
- Chart.js: luôn register components trước khi dùng (`Chart.register(...)`)
- React Router v6: dùng `<Outlet>`, `useNavigate`, KHÔNG dùng `useHistory`
- State: ưu tiên `useState`/`useContext`, tránh thêm Redux nếu không cần

## Mobile conventions
- Flutter 3.x, Dart null-safety bắt buộc (`?`, `!`, `late`)
- HTTP: dùng `dio` hoặc `http` package (kiểm tra pubspec.yaml trước)
- State: kiểm tra `pubspec.yaml` để biết đang dùng Provider/Riverpod/Bloc

## Database / Cache
- PostgreSQL 16: dùng `snake_case` cho tên bảng và cột
- Redis 7: Spring Cache annotations (`@Cacheable`, `@CacheEvict`)
- Không hardcode connection string — đọc từ `application.yml` / env vars

## AI Integration
- Model: `gpt-4o-mini` (tiết kiệm token)
- Gọi OpenAI qua backend, KHÔNG expose API key ra frontend/mobile

## Docker
- Khởi động local: `docker-compose up -d`
- Env vars lấy từ `.env` (copy từ `.env.example`)

## Quy tắc cho Claude
- KHÔNG giải thích lý thuyết, chỉ viết code
- KHÔNG thêm dependency mới nếu không được yêu cầu
- Khi sửa bug: chỉ trả về đoạn code thay đổi, kèm tên file + số dòng
- Khi tạo file mới: ghi rõ đường dẫn đầy đủ
- Trả lời bằng tiếng Việt nếu câu hỏi bằng tiếng Việt
