# 🔧 Hướng dẫn Cài đặt — Hệ thống Quản lý Công việc

Hướng dẫn từng bước để cài đặt và chạy toàn bộ hệ thống trên máy tính cục bộ.

---

## 📋 Yêu cầu hệ thống

| Phần mềm | Phiên bản tối thiểu |
|---|---|
| Java (JDK) | 17 trở lên |
| Maven | 3.9+ |
| Node.js | 18 trở lên |
| PostgreSQL | 14 trở lên |
| Flutter | 3.x (nếu phát triển ứng dụng di động) |
| Docker & Docker Compose | Phiên bản mới nhất (tùy chọn) |

---

## 🐘 Cài đặt PostgreSQL

### Trên Ubuntu/Debian

```bash
# Cài đặt PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Khởi động dịch vụ
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### Tạo cơ sở dữ liệu

```bash
# Đăng nhập với người dùng postgres
sudo -u postgres psql

# Tạo database và người dùng
CREATE DATABASE task_management;
CREATE USER tm_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE task_management TO tm_user;
\q
```

---

## ⚙️ Cấu hình Backend

Mở file `backend/src/main/resources/application.properties` và chỉnh sửa:

```properties
# Kết nối cơ sở dữ liệu
spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=tm_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Cấu hình JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Cấu hình JWT (bắt buộc set qua env, không có default)
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000

# Cổng máy chủ (project chạy trên 5000)
server.port=5000
```

---

## 🚀 Chạy Backend

```bash
# Di chuyển vào thư mục backend
cd backend

# Cài đặt phụ thuộc và chạy (lần đầu có thể lâu hơn)
./mvnw spring-boot:run
```

Kiểm tra API đang hoạt động:

```bash
# Gọi thử endpoint kiểm tra sức khỏe
curl http://localhost:8080/api/auth/login
```

---

## 🌐 Cấu hình Frontend

Mở file `frontend/src/api/axios.js` và kiểm tra URL backend:

```javascript
// Đảm bảo URL trỏ đúng đến backend
const BASE_URL = 'http://localhost:8080';
```

---

## 🖥️ Chạy Frontend

```bash
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt các gói npm
npm install

# Khởi động môi trường phát triển
npm run dev
```

Mở trình duyệt tại: **`http://localhost:5173`**

---

## 📱 Chạy Ứng dụng Di động (Flutter)

```bash
# Di chuyển vào thư mục mobile
cd mobile

# Lấy các gói phụ thuộc
flutter pub get

# Kiểm tra thiết bị kết nối
flutter devices

# Chạy trên thiết bị/trình giả lập
flutter run
```

---

## 🐳 Chạy bằng Docker Compose

Để khởi động toàn bộ hệ thống (backend + database) chỉ bằng một lệnh:

```bash
# Ở thư mục gốc của dự án
docker-compose up --build
```

Để chạy ở chế độ nền (background):

```bash
docker-compose up -d --build
```

Để dừng tất cả dịch vụ:

```bash
docker-compose down
```

---

## ✅ Kiểm tra cài đặt

Sau khi chạy xong, kiểm tra các địa chỉ sau:

| Dịch vụ | Địa chỉ |
|---|---|
| Backend API | `http://localhost:8080` |
| Giao diện Frontend | `http://localhost:5173` |
| PostgreSQL | `localhost:5432` |

---

## ❗ Xử lý sự cố thường gặp

**Lỗi kết nối cơ sở dữ liệu:**
- Kiểm tra PostgreSQL đang chạy: `sudo systemctl status postgresql`
- Xác nhận thông tin đăng nhập trong `application.properties`

**Lỗi thiếu port:**
- Đảm bảo không có ứng dụng nào đang chiếm port 8080 (backend) hoặc 5173 (frontend)
- Kiểm tra bằng: `lsof -i :8080` hoặc `lsof -i :5173`

**Frontend không kết nối được với backend:**
- Kiểm tra backend đang chạy tại `http://localhost:8080`
- Xem lại cấu hình `BASE_URL` trong `frontend/src/api/axios.js`
