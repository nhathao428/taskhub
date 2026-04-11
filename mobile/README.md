# 📱 Hệ thống Quản lý Công việc — Ứng dụng Di động

Ứng dụng di động cho Hệ thống Quản lý Công việc, xây dựng bằng **Flutter** và kết nối với backend Spring Boot qua REST API.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Framework | Flutter 3.x |
| Ngôn ngữ | Dart ≥ 3.0 |
| HTTP Client | `http` ^1.2.2 |
| State Management | `provider` ^6.1.2 |
| Local Storage | `shared_preferences` ^2.3.2 |

---

## 📋 Yêu cầu

- **Flutter SDK** 3.x trở lên — [Hướng dẫn cài đặt Flutter](https://docs.flutter.dev/get-started/install)
- **Android Studio** (để chạy Android Emulator) hoặc **Xcode** (để chạy iOS Simulator, cần macOS)
- Backend Spring Boot đang chạy tại `http://localhost:8080`

---

## 🚀 Cài đặt & Chạy

```bash
# 1. Di chuyển vào thư mục mobile
cd mobile

# 2. Tải các gói phụ thuộc
flutter pub get

# 3. Kiểm tra môi trường Flutter
flutter doctor

# 4. Chạy ứng dụng trên emulator hoặc thiết bị thật
flutter run
```

> **Lưu ý:** Đảm bảo backend đang chạy và có thể truy cập từ thiết bị / emulator trước khi khởi động ứng dụng.

---

## ✨ Tính năng

| Tính năng | Mô tả |
|---|---|
| 🔐 Đăng nhập / Đăng ký | Xác thực bằng JWT, lưu token cục bộ với SharedPreferences |
| 📊 Dashboard thống kê | Tổng quan nhân viên, dự án, công việc, chấm công hôm nay |
| 👥 Quản lý nhân viên | Xem danh sách, thêm nhân viên mới |
| 📂 Quản lý dự án | Xem danh sách, tạo dự án mới |
| 📋 Quản lý công việc | Xem danh sách, tạo và cập nhật trạng thái công việc |
| 🕐 Chấm công | Check-in / Check-out, xem lịch sử chấm công |
| 🤖 AI Gợi ý | Nhập tiêu đề + kỹ năng, nhận top 5 nhân viên phù hợp |

---

## 📁 Cấu trúc thư mục

```
mobile/
├── pubspec.yaml              # Cấu hình dự án và phụ thuộc
├── lib/
│   ├── main.dart             # Điểm vào ứng dụng, khai báo routes
│   ├── models/               # Data models
│   │   ├── user.dart
│   │   ├── employee.dart
│   │   ├── project.dart
│   │   ├── task.dart
│   │   ├── attendance.dart
│   │   └── employee_suggestion.dart
│   ├── services/             # API service
│   │   └── api_service.dart  # Tất cả HTTP requests + JWT interceptor
│   ├── providers/            # State management (Provider)
│   │   ├── auth_provider.dart
│   │   └── data_provider.dart
│   ├── screens/              # Các màn hình
│   │   ├── login_screen.dart
│   │   ├── register_screen.dart
│   │   ├── dashboard_screen.dart
│   │   ├── employees_screen.dart
│   │   ├── projects_screen.dart
│   │   ├── tasks_screen.dart
│   │   ├── attendance_screen.dart
│   │   └── ai_suggestions_screen.dart
│   └── widgets/              # Widget dùng chung
│       ├── loading_widget.dart
│       └── status_badge.dart
└── README.md
```

---

## 📦 Build & Phát hành

### Android

```bash
# Build APK (debug)
flutter build apk --debug

# Build APK (release)
flutter build apk --release
```

File APK sẽ nằm tại: `build/app/outputs/flutter-apk/app-release.apk`

### iOS (cần macOS + Xcode)

```bash
# Build cho iOS Simulator
flutter build ios --simulator
```

---

## 🔗 Kết nối Backend

Backend URL mặc định là `http://10.0.2.2:8080` (cho Android Emulator trỏ về localhost của máy host).

Để thay đổi URL khi build, truyền biến môi trường:

```bash
flutter run --dart-define=API_BASE_URL=http://192.168.1.x:8080
```

> **Khi dùng thiết bị thật:** Thay bằng địa chỉ IP thực của máy chạy backend trong cùng mạng LAN.