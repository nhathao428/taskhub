# Task Management System — Backend

The backend for the **Task Management System for Small Multi-Industry Enterprises** (Hệ thống quản lý công việc cho doanh nghiệp nhỏ đa ngành).

## Features

- **Attendance Tracking** (Quản lý chấm công) — Record and report employee check-in/check-out
- **Project Management** (Quản lý dự án) — Create and manage projects, assign teams
- **Task Progress & Deadline Management** (Quản lý tiến độ và thời hạn) — Track task status, deadlines, and completion rates
- **AI-powered Employee Assignment** (Gợi ý AI phân công nhân viên) — Suggest the best-fit employee for each task based on skills, work schedule, and past performance

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 25.0.2 |
| Framework | Spring Boot 3.5.0 |
| Build Tool | Maven 3.9.14 |
| Database | PostgreSQL |
| Auth | Spring Security + JWT (jjwt 0.12.x) |
| ORM | Spring Data JPA / Hibernate |

## Getting Started

```bash
# From the backend/ directory
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

See `docs/SETUP_GUIDE.md` for full setup instructions including database configuration.
See `docs/API_SPECIFICATION.md` for REST API endpoints.
