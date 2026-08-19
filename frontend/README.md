# 🌐 Hệ thống Quản lý Công việc — Frontend

Giao diện người dùng cho Hệ thống Quản lý Công việc, xây dựng bằng React + Vite + Tailwind CSS.

---

## 🛠️ Công nghệ sử dụng

- **Vite** + **React 18** — framework và công cụ build
- **Tailwind CSS** — tạo kiểu giao diện
- **React Router DOM v6** — điều hướng trang
- **Axios** — gọi HTTP kèm JWT interceptor tự động
- **Chart.js + react-chartjs-2** — biểu đồ bảng điều khiển
- **React Icons** — bộ icon giao diện

---

## 📋 Yêu cầu

- Node.js ≥ 18
- Backend Spring Boot đang chạy tại `http://localhost:5000`

---

## ⚙️ Biến môi trường

Tạo file `.env` trong thư mục `frontend/`:

```env
VITE_API_BASE_URL=http://localhost:5000
```

> Nếu không có file `.env`, mặc định Axios sẽ gọi `http://localhost:5000`.

---

## 🚀 Cài đặt & Chạy

```bash
cd frontend

# Cài đặt phụ thuộc
npm install

# Chạy môi trường phát triển
npm run dev
```

Mở trình duyệt tại: **http://localhost:5173**

---

## 📦 Build Production

```bash
# Build tối ưu hoá cho production → thư mục dist/
npm run build

# Xem trước bản build production cục bộ
npm run preview
```

---

## 🗺️ Các trang

| Đường dẫn | Mô tả |
|---|---|
| `/login` | Đăng nhập |
| `/register` | Đăng ký tài khoản |
| `/dashboard` | Bảng điều khiển — thống kê + biểu đồ |
| `/employees` | Quản lý nhân viên (CRUD) |
| `/projects` | Quản lý dự án (CRUD) |
| `/tasks` | Quản lý công việc (CRUD) |
| `/attendance` | Chấm công (vào/ra theo ngày) |
| `/ai-suggestions` | AI gợi ý nhân viên phù hợp |

---

## 📁 Cấu trúc thư mục

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── .env                        # Biến môi trường (tự tạo)
└── src/
    ├── main.jsx                # Điểm vào ứng dụng
    ├── App.jsx                 # Router và cấu hình routes
    ├── index.css               # CSS toàn cục + Tailwind directives
    ├── api/
    │   └── axios.js            # Axios instance + JWT interceptor tự động
    ├── components/
    │   ├── Layout.jsx          # Khung bố cục chung (Sidebar + nội dung)
    │   ├── Sidebar.jsx         # Thanh điều hướng bên trái
    │   ├── ProtectedRoute.jsx  # Bảo vệ route — yêu cầu đăng nhập
    │   └── Modal.jsx           # Component modal dùng chung
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
        └── AuthContext.jsx     # Context quản lý trạng thái đăng nhập toàn cục
```

---

## 📸 Ảnh chụp màn hình

> *(Placeholder — thêm ảnh chụp màn hình sau khi hoàn thiện UI)*

| Trang | Mô tả |
|---|---|
| Dashboard | Biểu đồ thống kê tổng quan |
| Quản lý nhân viên | Danh sách + form thêm/sửa nhân viên |
| AI gợi ý | Form nhập công việc và kết quả đề xuất từ AI |