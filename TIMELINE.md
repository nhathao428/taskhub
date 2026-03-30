# 🗓️ Kế hoạch Phát triển 10 Tuần — Hệ thống Quản lý Công việc

---

## ⏱️ Tuần 1: Nền tảng Backend

- Thiết lập cấu trúc dự án Spring Boot và môi trường phát triển.
- Cấu hình PostgreSQL và thiết kế lược đồ cơ sở dữ liệu ban đầu.
- Khởi tạo Maven, cài đặt các phụ thuộc cần thiết (Spring Data JPA, Spring Security, jjwt).

---

## 🔐 Tuần 2: Xác thực

- Xây dựng API đăng ký (`POST /api/auth/register`) và đăng nhập (`POST /api/auth/login`).
- Thiết lập JWT để xác thực và phân quyền người dùng.
- Cấu hình Spring Security với bộ lọc JWT.

---

## 📋 Tuần 3: Dự án & Công việc

- Phát triển API CRUD đầy đủ cho dự án (`/api/projects`).
- Xây dựng API CRUD cho công việc (`/api/tasks`), bao gồm phân công nhân viên.
- Thiết lập mối quan hệ giữa dự án, công việc và nhân viên trong cơ sở dữ liệu.

---

## 🕐 Tuần 4: Chấm công

- Thiết kế hệ thống theo dõi chấm công theo ngày.
- Phát triển API chấm công vào (`POST /api/attendance/checkin`) và ra (`POST /api/attendance/checkout`).
- Xây dựng chức năng xem lịch sử và báo cáo chấm công.

---

## 🖥️ Tuần 5: Frontend (Phần 1)

- Thiết lập dự án React + Vite + Tailwind CSS.
- Tạo wireframe và thiết kế UI/UX cho các trang chính.
- Xây dựng cấu trúc điều hướng (React Router DOM v6) và bố cục chung (Layout, Sidebar).
- Hoàn thiện trang đăng nhập và đăng ký.

---

## 🔗 Tuần 6: Frontend (Phần 2)

- Tích hợp Axios với JWT interceptor để gọi API backend.
- Xây dựng trang quản lý nhân viên, dự án, công việc (CRUD hoàn chỉnh).
- Xây dựng trang chấm công và bảng điều khiển thống kê với Chart.js.

---

## 📱 Tuần 7: Ứng dụng Di động

- Thiết lập cấu trúc dự án Flutter.
- Phát triển màn hình đăng nhập và điều hướng cơ bản.
- Tích hợp API backend cho các tính năng quản lý dự án và công việc trên thiết bị di động.

---

## 🤖 Tuần 8: Bộ máy AI Gợi ý

- Nghiên cứu và xây dựng thuật toán tính điểm tổng hợp (kỹ năng 35%, khối lượng 25%, hiệu suất 25%, chấm công 15%).
- Phát triển `AiSuggestionService` với caching Spring Cache.
- Tích hợp API gợi ý (`POST /api/suggestions/recommend`) và chức năng phản hồi.

---

## 🚀 Tuần 9: Triển khai

- Thiết lập pipeline CI/CD cho kiểm thử và triển khai tự động.
- Cấu hình Docker và Docker Compose cho toàn bộ hệ thống.
- Triển khai ứng dụng lên nền tảng cloud.

---

## 🧪 Tuần 10: Kiểm thử và Phản hồi

- Kiểm thử toàn bộ chức năng (kiểm thử đơn vị, kiểm thử tích hợp, kiểm thử người dùng).
- Tối ưu hiệu năng và sửa lỗi phát sinh.
- Thu thập phản hồi từ người dùng thực và cải tiến hệ thống dựa trên ý kiến đóng góp.
