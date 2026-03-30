# 📱 Hệ thống Quản lý Công việc — Ứng dụng Di động

Ứng dụng di động cho Hệ thống Quản lý Công việc, xây dựng bằng **Flutter** và kết nối với backend Spring Boot qua REST API.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Framework | Flutter 3.x |
| Ngôn ngữ | Dart ≥ 3.0 |
| HTTP Client | `http` ^1.1.0 |
| State Management | `get` (GetX) ^4.6.6 |
| Local Storage | `hive` + `hive_flutter` ^2.2.3 |

---

## 📋 Yêu cầu

- **Flutter SDK** 3.x trở lên — [Hướng dẫn cài đặt Flutter](https://docs.flutter.dev/get-started/install)
- **Android Studio** (để chạy Android Emulator) hoặc **Xcode** (để chạy iOS Simulator, cần macOS)
- Backend Spring Boot đang chạy tại `http://localhost:5000`

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
| 🔐 Đăng nhập / Đăng ký | Xác thực bằng JWT, lưu token cục bộ với Hive |
| 📋 Quản lý công việc | Xem danh sách, tạo và cập nhật công việc |
| 📂 Quản lý dự án | Xem danh sách và chi tiết dự án |
| 🕐 Chấm công | Ghi nhận giờ vào/ra từ thiết bị di động |
| 👥 Danh sách nhân viên | Xem thông tin nhân viên |

---

## 📁 Cấu trúc thư mục

```
mobile/
├── pubspec.yaml              # Cấu hình dự án và phụ thuộc
├── pubspec.lock
├── lib/
│   ├── main.dart             # Điểm vào ứng dụng
│   ├── app/
│   │   ├── routes/           # Cấu hình điều hướng (GetX)
│   │   └── bindings/         # Dependency injection
│   ├── core/
│   │   ├── api/              # HTTP client + interceptors
│   │   └── storage/          # Hive local storage (JWT token)
│   ├── features/
│   │   ├── auth/             # Đăng nhập, đăng ký
│   │   ├── tasks/            # Quản lý công việc
│   │   ├── projects/         # Quản lý dự án
│   │   ├── employees/        # Danh sách nhân viên
│   │   └── attendance/       # Chấm công
│   └── shared/
│       └── widgets/          # Các widget dùng chung
└── android/                  # Cấu hình Android
└── ios/                      # Cấu hình iOS
```

---

## 📦 Build & Phát hành

### Android

```bash
# Build APK (debug)
flutter build apk --debug

# Build APK (release)
flutter build apk --release

# Build Android App Bundle (dùng cho Google Play)
flutter build appbundle --release
```

File APK sẽ nằm tại: `build/app/outputs/flutter-apk/app-release.apk`

### iOS (cần macOS + Xcode)

```bash
# Build cho iOS Simulator
flutter build ios --simulator

# Build cho thiết bị thật (cần Apple Developer Account)
flutter build ios --release
```

---

## 🔗 Kết nối Backend

Cấu hình URL backend trong file cấu hình API của ứng dụng (ví dụ: `lib/core/api/api_client.dart`):

```dart
const String baseUrl = 'http://localhost:5000'; // Thay bằng IP thực khi dùng thiết bị thật
```

> **Khi dùng Android Emulator:** Thay `localhost` bằng `10.0.2.2` để trỏ vào máy host.
> **Khi dùng thiết bị thật:** Sử dụng địa chỉ IP thực của máy chạy backend trong cùng mạng LAN.