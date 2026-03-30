# 🌐 Hệ thống Quản lý Công việc — Frontend

Giao diện người dùng cho Hệ thống Quản lý Công việc, xây dựng bằng React + Vite + Tailwind CSS.

## 🛠️ Công nghệ sử dụng

- **Vite** + **React 18**
- **Tailwind CSS** — tạo kiểu giao diện
- **React Router DOM v6** — điều hướng trang
- **Axios** — gọi HTTP kèm JWT interceptor tự động
- **Chart.js + react-chartjs-2** — biểu đồ bảng điều khiển
- **React Icons** — bộ icon giao diện

## Yêu cầu

- Node.js ≥ 18
- Backend Spring Boot chạy trên `http://localhost:5000`

## Cài đặt & Chạy

```bash
cd frontend
npm install
npm run dev
```

Mở trình duyệt tại: **http://localhost:5173**

## Các trang

| Đường dẫn | Mô tả |
|---|---|
| `/login` | Đăng nhập |
| `/register` | Đăng ký tài khoản |
| `/dashboard` | Bảng điều khiển — thống kê + biểu đồ |
| `/employees` | Quản lý nhân viên (CRUD) |
| `/projects` | Quản lý dự án (CRUD) |
| `/tasks` | Quản lý công việc (CRUD) |
| `/attendance` | Chấm công |
| `/ai-suggestions` | AI gợi ý nhân viên phù hợp |

## Cấu trúc thư mục

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── index.css
    ├── api/
    │   └── axios.js           # Axios instance + JWT interceptor
    ├── components/
    │   ├── Layout.jsx
    │   ├── Sidebar.jsx
    │   ├── ProtectedRoute.jsx
    │   └── Modal.jsx
    ├── pages/
    │   ├── Login.jsx
    │   ├── Register.jsx
    │   ├── Dashboard.jsx
    │   ├── Employees.jsx
    │   ├── Projects.jsx
    │   ├── Tasks.jsx
    │   ├── Attendance.jsx
    │   └── AiSuggestions.jsx
    └── context/
        └── AuthContext.jsx
```