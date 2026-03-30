# 🗂️ Hệ thống Quản lý Công việc cho Doanh nghiệp Nhỏ Đa ngành

Hệ thống quản lý toàn diện giúp doanh nghiệp nhỏ quản lý nhân sự, chấm công, dự án, tiến độ công việc và nhận gợi ý phân công nhân viên thông minh từ AI (OpenAI GPT).

---

## 🏗️ Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────┐
│                      CLIENT LAYER                       │
│   ┌──────────────────┐       ┌─────────────────────┐   │
│   │  React + Vite    │       │   Flutter Mobile     │   │
│   │  (port 5173)     │       │  (Android / iOS)     │   │
│   └────────┬─────────┘       └──────────┬──────────┘   │
└────────────┼──────────────────────────────┼─────────────┘
             │         HTTP / REST API       │
             ▼                               ▼
┌─────────────────────────────────────────────────────────┐
│                     BACKEND LAYER                       │
│         Spring Boot REST API (port 5000)                │
│    JWT Auth │ CRUD APIs │ AI Suggestion (OpenAI)        │
└──────────┬─────────────────────────┬────────────────────┘
           │                         │
           ▼                         ▼
┌──────────────────┐     ┌──────────────────────┐
│  PostgreSQL 16   │     │     Redis 7           │
│  (port 5432)     │     │  (port 6379 / cache)  │
└──────────────────┘     └──────────────────────┘
```

---

## ✨ Tính năng chính

| Tính năng | Mô tả |
|---|---|
| 🔐 Xác thực JWT | Đăng ký, đăng nhập, phân quyền theo vai trò (Admin / Employee) |
| 👥 Quản lý nhân viên | CRUD nhân viên, quản lý kỹ năng (skill + mức độ thành thạo) |
| 📋 Quản lý dự án | Tạo, cập nhật, xóa dự án; liên kết với công việc |
| ✅ Quản lý công việc | Tạo công việc, phân công nhân viên, theo dõi trạng thái |
| 🕐 Chấm công | Ghi nhận giờ vào/ra theo ngày, xem lịch sử chấm công |
| 🤖 AI Gợi ý nhân viên | Tích hợp OpenAI GPT để đề xuất top 5 nhân viên phù hợp nhất |
| 📊 Dashboard | Biểu đồ thống kê tổng quan (Chart.js) |

---

## 🛠️ Công nghệ sử dụng

| Tầng | Công nghệ | Phiên bản |
|---|---|---|
| **Backend** | Java, Spring Boot, Maven | Java 17+, Spring Boot 3.5.0 |
| **Xác thực** | Spring Security + JWT (jjwt) | 0.12.x |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Frontend** | React, Vite, Tailwind CSS | React 18, Vite 5 |
| **HTTP Client** | Axios + JWT interceptor | - |
| **Biểu đồ** | Chart.js + react-chartjs-2 | - |
| **Routing** | React Router DOM | v6 |
| **Mobile** | Flutter, Dart | Flutter 3.x, Dart ≥ 3.0 |
| **Cơ sở dữ liệu** | PostgreSQL | 16 |
| **Cache** | Redis + Spring Cache | 7 |
| **Container** | Docker, Docker Compose | - |
| **AI** | OpenAI GPT API | gpt-4o-mini |

---

## 📋 Yêu cầu hệ thống

- **Java** 17 trở lên
- **Node.js** 18 trở lên và **npm**
- **Docker** & **Docker Compose** (để chạy PostgreSQL và Redis)
- **Flutter SDK** 3.x trở lên (nếu phát triển mobile)
- **Android Studio** hoặc **Xcode** (để chạy emulator / simulator)

---

## 🚀 Khởi động nhanh với Docker Compose

```bash
# 1. Clone repository
git clone https://github.com/nhathao428/task-management-system.git
cd task-management-system

# 2. Tạo file biến môi trường (tuỳ chọn)
cp .env.example .env   # chỉnh JWT_SECRET nếu cần

# 3. Khởi động PostgreSQL, Redis và Backend
docker-compose up --build -d

# 4. Cài đặt và chạy Frontend riêng
cd frontend
npm install
npm run dev
```

- **API Backend**: `http://localhost:5000`
- **Giao diện Frontend**: `http://localhost:5173`

---

## 🔧 Cài đặt thủ công

### Backend (Spring Boot)

```bash
cd backend

# Cấu hình database trong src/main/resources/application.properties
# Xem mục "Cấu hình Backend" bên dưới

./mvnw spring-boot:run
```

API sẽ khởi động tại: `http://localhost:5000`

#### Cấu hình Backend (`application.properties`)

```properties
# Kết nối PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/task_management_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=mySecretKeyThatIsAtLeast256BitsLong1234567890

# Redis Cache
spring.redis.host=localhost
spring.redis.port=6379

# OpenAI (cho tính năng AI gợi ý)
openai.api.key=sk-...
openai.model=gpt-4o-mini

# Server port
server.port=5000
```

### Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev          # Môi trường phát triển: http://localhost:5173
npm run build        # Build production → dist/
npm run preview      # Xem trước bản build production
```

#### Biến môi trường Frontend (`.env`)

```env
VITE_API_BASE_URL=http://localhost:5000
```

### Mobile (Flutter)

```bash
cd mobile
flutter pub get
flutter run           # Chạy trên emulator / thiết bị thật
flutter build apk     # Build Android APK
flutter build ios     # Build iOS (cần macOS + Xcode)
```

---

## 📡 Tổng quan API Endpoints

| Phương thức | Đường dẫn | Mô tả | Yêu cầu Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Đăng ký tài khoản | Không |
| `POST` | `/api/auth/login` | Đăng nhập, nhận JWT | Không |
| `GET` | `/api/employees` | Danh sách nhân viên | ✅ |
| `POST` | `/api/employees` | Thêm nhân viên | ✅ |
| `PUT` | `/api/employees/{id}` | Cập nhật nhân viên | ✅ |
| `DELETE` | `/api/employees/{id}` | Xóa nhân viên | ✅ |
| `GET` | `/api/projects` | Danh sách dự án | ✅ |
| `POST` | `/api/projects` | Tạo dự án mới | ✅ |
| `PUT` | `/api/projects/{id}` | Cập nhật dự án | ✅ |
| `DELETE` | `/api/projects/{id}` | Xóa dự án | ✅ |
| `GET` | `/api/tasks` | Danh sách công việc | ✅ |
| `POST` | `/api/tasks` | Tạo công việc mới | ✅ |
| `PUT` | `/api/tasks/{id}` | Cập nhật công việc | ✅ |
| `DELETE` | `/api/tasks/{id}` | Xóa công việc | ✅ |
| `POST` | `/api/attendance/checkin` | Chấm công vào | ✅ |
| `POST` | `/api/attendance/checkout` | Chấm công ra | ✅ |
| `GET` | `/api/attendance` | Lịch sử chấm công | ✅ |
| `POST` | `/api/suggestions/recommend` | AI gợi ý nhân viên | ✅ |
| `POST` | `/api/suggestions/recommend/{taskId}` | AI gợi ý theo task ID | ✅ |

> Xem đặc tả đầy đủ tại [docs/API_SPECIFICATION.md](docs/API_SPECIFICATION.md)

---

## 📁 Cấu trúc dự án

```
task-management-system/
├── backend/                        # Spring Boot REST API
│   ├── src/main/java/com/example/taskmanagement/
│   │   ├── config/                 # Security, CORS, Redis, OpenAPI
│   │   ├── controller/             # REST Controllers
│   │   ├── dto/                    # Data Transfer Objects
│   │   ├── entity/                 # JPA Entities (User, Employee, Task…)
│   │   ├── repository/             # Spring Data JPA Repositories
│   │   ├── security/               # JWT Filter & Utility
│   │   └── service/                # Business Logic + AI Service
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                       # React + Vite + Tailwind CSS
│   ├── src/
│   │   ├── api/axios.js            # Axios instance + JWT interceptor
│   │   ├── components/             # Layout, Sidebar, Modal, ProtectedRoute
│   │   ├── context/AuthContext.jsx # Auth state toàn cục
│   │   └── pages/                  # Login, Dashboard, Employees, …
│   └── package.json
├── mobile/                         # Flutter mobile app
│   └── pubspec.yaml
├── docs/                           # Tài liệu kỹ thuật
│   ├── API_SPECIFICATION.md
│   ├── DATABASE_SCHEMA.md
│   ├── SETUP_GUIDE.md
│   └── UML_DIAGRAMS.md
├── docker-compose.yml              # PostgreSQL + Redis + Backend
├── TIMELINE.md                     # Kế hoạch phát triển 10 tuần
└── README.md
```

---

## 🤖 Tính năng AI Gợi ý Nhân viên

Hệ thống tích hợp **OpenAI GPT** để phân tích và đề xuất nhân viên phù hợp nhất cho từng công việc.

### Cách hoạt động

1. Frontend gửi thông tin công việc (`tiêu đề`, `mô tả`, `kỹ năng yêu cầu`) đến backend.
2. Backend thu thập dữ liệu thực tế của toàn bộ nhân viên:
   - Kỹ năng và mức độ thành thạo
   - Số lượng task đang thực hiện (workload)
   - Tỷ lệ hoàn thành task
   - Số ngày chấm công trong 30 ngày gần nhất
3. Toàn bộ dữ liệu được gửi cho OpenAI GPT dưới dạng ngữ cảnh (prompt).
4. AI phân tích và trả về **top 5 nhân viên** phù hợp nhất kèm điểm đánh giá và lý do.

### Ví dụ Request

```json
POST /api/suggestions/recommend
{
  "taskTitle": "Phát triển API thanh toán",
  "taskDescription": "Xây dựng REST API tích hợp cổng thanh toán VNPay",
  "requiredSkills": ["Java", "Spring Boot", "REST API"]
}
```

### Ví dụ Response

```json
[
  {
    "employeeId": 3,
    "firstName": "Nguyễn",
    "lastName": "Văn A",
    "department": "Engineering",
    "skillMatchScore": 0.95,
    "workloadScore": 0.80,
    "performanceScore": 0.90,
    "attendanceScore": 0.85,
    "overallScore": 0.88,
    "reasoning": "Nhân viên có kỹ năng Java và Spring Boot xuất sắc, workload hiện tại thấp, tỷ lệ hoàn thành task cao."
  }
]
```

---

## 📖 Tài liệu

| Tài liệu | Mô tả |
|---|---|
| [Đặc tả API](docs/API_SPECIFICATION.md) | Danh sách tất cả API endpoints kèm mô tả và ví dụ |
| [Lược đồ Cơ sở dữ liệu](docs/DATABASE_SCHEMA.md) | Cấu trúc bảng và quan hệ dữ liệu |
| [Hướng dẫn Cài đặt](docs/SETUP_GUIDE.md) | Hướng dẫn cài đặt môi trường chi tiết |
| [Sơ đồ UML](docs/UML_DIAGRAMS.md) | Sơ đồ Use Case và Sequence |
| [Kế hoạch Phát triển](TIMELINE.md) | Lộ trình phát triển 10 tuần |
| [Backend](backend/README.md) | Tài liệu chi tiết về phần backend |
| [Frontend](frontend/README.md) | Tài liệu chi tiết về phần frontend |
| [Mobile](mobile/README.md) | Tài liệu chi tiết về ứng dụng di động |

---

## 📄 Giấy phép

Dự án được phát triển cho mục đích học thuật. Mọi đóng góp và phản hồi đều được hoan nghênh.
