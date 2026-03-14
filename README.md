# Task Management System
**Hệ thống quản lý công việc cho doanh nghiệp nhỏ đa ngành**

A comprehensive task management system designed for small multi-industry enterprises, featuring attendance tracking, project management, task deadline monitoring, and AI-powered employee assignment suggestions.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🕐 Attendance Tracking | Record and report employee check-in/check-out (Quản lý chấm công) |
| 📁 Project Management | Create and manage projects, assign teams (Quản lý dự án) |
| ✅ Task Progress & Deadlines | Track task status, progress, and deadlines (Quản lý tiến độ và thời hạn) |
| 🤖 AI Employee Suggestions | AI recommends the best employee for each task based on skills, schedule, and past performance (Gợi ý nhân viên phù hợp bằng AI) |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25.0.2, Spring Boot 3.5.0, Maven 3.9.14 |
| Database | PostgreSQL |
| Auth | Spring Security + JWT |
| Frontend | React 18, Vite, Tailwind CSS, Redux Toolkit |
| Mobile | Flutter 3.x |

---

## 🚀 Quick Start

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
# API available at http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# UI available at http://localhost:5173
```

### Mobile
```bash
cd mobile
flutter pub get
flutter run
```

---

## 📁 Project Structure

```
task-management-system/
├── backend/          # Spring Boot REST API (Java 25, Maven)
├── frontend/         # React + Vite web application
├── mobile/           # Flutter mobile application
├── docs/             # API specs, database schema, setup guide
│   ├── API_SPECIFICATION.md
│   ├── DATABASE_SCHEMA.md
│   └── SETUP_GUIDE.md
└── TIMELINE.md       # Project development timeline
```

---

## 📖 Documentation

- [Setup Guide](docs/SETUP_GUIDE.md)
- [API Specification](docs/API_SPECIFICATION.md)
- [Database Schema](docs/DATABASE_SCHEMA.md)
