# 🖥️ Hệ thống Quản lý Công việc — Backend

Phần backend của Hệ thống Quản lý Công việc, xây dựng bằng Spring Boot và cung cấp REST API cho frontend và ứng dụng di động.

---

## ✨ Tính năng

- Xác thực người dùng bằng JWT (đăng ký, đăng nhập)
- CRUD đầy đủ cho nhân viên, dự án và công việc
- Theo dõi chấm công (vào/ra theo ngày)
- AI gợi ý nhân viên phù hợp dựa trên điểm tổng hợp 4 tiêu chí
- Phân quyền dựa trên vai trò (Quản lý / Nhân viên)
- Caching với Spring Cache để tối ưu hiệu năng

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 25.0.2 |
| Framework | Spring Boot 3.5.0 |
| Công cụ build | Maven 3.9.14 |
| Cơ sở dữ liệu | PostgreSQL |
| Xác thực | Spring Security + JWT (jjwt 0.12.x) |
| ORM | Spring Data JPA / Hibernate |
| Caching | Spring Cache |

---

## 🚀 Hướng dẫn chạy

### Yêu cầu

- Java 17 trở lên
- Maven 3.9+
- PostgreSQL đang chạy

### Cấu hình

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
# Cấu hình kết nối cơ sở dữ liệu
spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=postgres
spring.datasource.password=your_password

# Khóa bí mật JWT
jwt.secret=your_jwt_secret_key

# Tự động cập nhật schema
spring.jpa.hibernate.ddl-auto=update
```

### Chạy ứng dụng

```bash
# Di chuyển vào thư mục backend
cd backend

# Chạy bằng Maven Wrapper
./mvnw spring-boot:run
```

API sẽ khởi động tại: **`http://localhost:5000`**

---

## 📡 API Endpoints chính

| Phương thức | Đường dẫn | Mô tả |
|---|---|---|
| `POST` | `/api/auth/register` | Đăng ký tài khoản mới |
| `POST` | `/api/auth/login` | Đăng nhập, nhận JWT |
| `GET` | `/api/employees` | Danh sách nhân viên |
| `POST` | `/api/employees` | Thêm nhân viên mới |
| `PUT` | `/api/employees/{id}` | Cập nhật nhân viên |
| `DELETE` | `/api/employees/{id}` | Xóa nhân viên |
| `GET` | `/api/projects` | Danh sách dự án |
| `POST` | `/api/tasks` | Tạo công việc mới |
| `POST` | `/api/attendance/checkin` | Chấm công vào |
| `POST` | `/api/attendance/checkout` | Chấm công ra |
| `POST` | `/api/suggestions/recommend` | AI gợi ý nhân viên |

> Xem đặc tả API đầy đủ tại [docs/API_SPECIFICATION.md](../docs/API_SPECIFICATION.md)

---

## 🤖 Thuật toán AI Gợi ý

Mỗi nhân viên được tính điểm tổng hợp từ 4 tiêu chí:

| Tiêu chí | Trọng số |
|---|---|
| Kỹ năng phù hợp | 35% |
| Khối lượng công việc hiện tại | 25% |
| Hiệu suất hoàn thành đúng hạn | 25% |
| Tỷ lệ chấm công (30 ngày gần nhất) | 15% |

Kết quả trả về **top 5** nhân viên có điểm cao nhất.
