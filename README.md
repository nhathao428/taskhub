# 🗂️ Hệ thống Quản lý Công việc cho Doanh nghiệp Nhỏ Đa ngành

Hệ thống quản lý toàn diện giúp doanh nghiệp nhỏ quản lý chấm công, dự án, tiến độ công việc và nhận gợi ý phân công nhân viên thông minh từ AI.

---

## ✨ Tính năng chính

| Tính năng | Mô tả |
|---|---|
| 👥 Quản lý nhân viên | Thêm, sửa, xóa thông tin nhân viên và kỹ năng |
| 📋 Quản lý dự án & công việc | Tạo dự án, phân công công việc, theo dõi tiến độ |
| 🕐 Chấm công | Ghi nhận giờ vào/ra, xem lịch sử, báo cáo chấm công |
| 🤖 AI Gợi ý nhân viên | Đề xuất top 5 nhân viên phù hợp nhất dựa trên kỹ năng, khối lượng công việc, hiệu suất và chấm công |

---

## 🛠️ Công nghệ sử dụng

| Tầng | Công nghệ |
|---|---|
| Backend | Java 25.0.2, Spring Boot 3.5.0, Maven 3.9.14 |
| Cơ sở dữ liệu | PostgreSQL |
| Xác thực | Spring Security + JWT (jjwt 0.12.x) |
| ORM | Spring Data JPA / Hibernate |
| Frontend | React 18, Vite, Tailwind CSS, React Router DOM v6, Axios, Chart.js |
| Di động | Flutter 3.x |

---

## 🚀 Hướng dẫn chạy nhanh

### Backend (Spring Boot)

```bash
# Di chuyển vào thư mục backend
cd backend

# Chạy ứng dụng Spring Boot
./mvnw spring-boot:run
```

API sẽ chạy tại: `http://localhost:8080`

### Frontend (React + Vite)

```bash
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt thư viện
npm install

# Khởi động môi trường phát triển
npm run dev
```

Giao diện sẽ chạy tại: `http://localhost:5173`

### Di động (Flutter)

```bash
# Di chuyển vào thư mục mobile
cd mobile

# Lấy các gói phụ thuộc
flutter pub get

# Chạy ứng dụng
flutter run
```

### Docker Compose (chạy toàn bộ hệ thống)

```bash
# Khởi động tất cả dịch vụ bằng Docker
docker-compose up --build
```

---

## 📁 Cấu trúc dự án

```
task-management-system/
├── backend/          # Spring Boot REST API
├── frontend/         # React + Vite
├── mobile/           # Flutter
├── docs/             # Tài liệu
│   ├── API_SPECIFICATION.md
│   ├── DATABASE_SCHEMA.md
│   ├── SETUP_GUIDE.md
│   └── UML_DIAGRAMS.md
├── docker-compose.yml
└── TIMELINE.md
```

---

## 📖 Tài liệu

| Tài liệu | Mô tả |
|---|---|
| [Đặc tả API](docs/API_SPECIFICATION.md) | Danh sách tất cả API endpoints kèm mô tả và ví dụ |
| [Lược đồ Cơ sở dữ liệu](docs/DATABASE_SCHEMA.md) | Cấu trúc bảng và quan hệ dữ liệu |
| [Hướng dẫn Cài đặt](docs/SETUP_GUIDE.md) | Hướng dẫn cài đặt môi trường và chạy dự án |
| [Sơ đồ UML](docs/UML_DIAGRAMS.md) | Sơ đồ Use Case và Sequence |
| [Kế hoạch Phát triển](TIMELINE.md) | Lộ trình phát triển 10 tuần |
| [Backend](backend/README.md) | Tài liệu chi tiết về phần backend |
| [Frontend](frontend/README.md) | Tài liệu chi tiết về phần frontend |
