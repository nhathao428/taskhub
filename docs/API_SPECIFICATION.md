# 📡 Đặc tả API — Hệ thống Quản lý Công việc

Tài liệu mô tả tất cả các API endpoint của hệ thống. Base URL: `http://localhost:8080`

> **Lưu ý xác thực:** Tất cả các endpoint (trừ đăng ký / đăng nhập) đều yêu cầu header:
> `Authorization: Bearer <JWT_TOKEN>`

---

## 1. 🔐 Xác thực

### Đăng ký tài khoản

**`POST /api/auth/register`**

Tạo tài khoản người dùng mới.

**Yêu cầu (Request Body):**
```json
{
  "username": "nguyenvana",
  "password": "matkhau123",
  "role": "EMPLOYEE"
}
```

**Phản hồi thành công (201):**
```json
{
  "message": "Đăng ký thành công",
  "userId": 1
}
```

---

### Đăng nhập

**`POST /api/auth/login`**

Xác thực người dùng và nhận JWT token.

**Yêu cầu (Request Body):**
```json
{
  "username": "nguyenvana",
  "password": "matkhau123"
}
```

**Phản hồi thành công (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "nguyenvana",
  "role": "EMPLOYEE"
}
```

**Phản hồi lỗi (401):**
```json
{
  "error": "Tên đăng nhập hoặc mật khẩu không đúng"
}
```

---

## 2. 👥 Nhân viên

### Lấy danh sách nhân viên

**`GET /api/employees`**

Trả về danh sách tất cả nhân viên.

**Phản hồi thành công (200):**
```json
[
  {
    "employeeId": 1,
    "firstName": "Văn A",
    "lastName": "Nguyễn",
    "email": "nguyenvana@company.com",
    "phone": "0901234567",
    "department": "Kỹ thuật",
    "position": "Lập trình viên",
    "hireDate": "2023-01-15",
    "status": "ACTIVE"
  }
]
```

---

### Lấy thông tin một nhân viên

**`GET /api/employees/{id}`**

**Phản hồi thành công (200):** Trả về đối tượng nhân viên như trên.

**Phản hồi lỗi (404):**
```json
{
  "error": "Không tìm thấy nhân viên với ID: 1"
}
```

---

### Thêm nhân viên mới

**`POST /api/employees`**

**Yêu cầu (Request Body):**
```json
{
  "firstName": "Thị B",
  "lastName": "Trần",
  "email": "tranthib@company.com",
  "phone": "0912345678",
  "department": "Kinh doanh",
  "position": "Nhân viên bán hàng",
  "hireDate": "2024-03-01",
  "status": "ACTIVE"
}
```

**Phản hồi thành công (201):** Trả về đối tượng nhân viên vừa tạo.

---

### Cập nhật nhân viên

**`PUT /api/employees/{id}`**

**Yêu cầu (Request Body):** Tương tự tạo mới.

**Phản hồi thành công (200):** Trả về đối tượng nhân viên đã cập nhật.

---

### Xóa nhân viên

**`DELETE /api/employees/{id}`**

**Phản hồi thành công (204 No Content)**

---

## 3. 📁 Dự án

### Lấy danh sách dự án

**`GET /api/projects`**

**Phản hồi thành công (200):**
```json
[
  {
    "projectId": 1,
    "name": "Hệ thống Quản lý Kho",
    "description": "Dự án xây dựng phần mềm quản lý kho hàng",
    "startDate": "2024-01-01",
    "endDate": "2024-06-30",
    "status": "IN_PROGRESS",
    "createdBy": 1
  }
]
```

---

### Thêm dự án mới

**`POST /api/projects`**

**Yêu cầu (Request Body):**
```json
{
  "name": "Website thương mại điện tử",
  "description": "Xây dựng cổng thương mại điện tử cho doanh nghiệp",
  "startDate": "2024-07-01",
  "endDate": "2024-12-31",
  "status": "PENDING"
}
```

**Phản hồi thành công (201):** Trả về đối tượng dự án vừa tạo.

---

### Cập nhật dự án

**`PUT /api/projects/{id}`**

**Phản hồi thành công (200):** Trả về đối tượng dự án đã cập nhật.

---

### Xóa dự án

**`DELETE /api/projects/{id}`**

**Phản hồi thành công (204 No Content)**

---

## 4. ✅ Công việc

### Lấy danh sách công việc

**`GET /api/tasks`**

**Phản hồi thành công (200):**
```json
[
  {
    "taskId": 1,
    "title": "Thiết kế cơ sở dữ liệu",
    "description": "Vẽ ERD và tạo schema PostgreSQL",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "dueDate": "2024-08-15",
    "projectId": 1,
    "assignedTo": 2
  }
]
```

---

### Tạo công việc mới

**`POST /api/tasks`**

**Yêu cầu (Request Body):**
```json
{
  "title": "Viết API xác thực",
  "description": "Xây dựng endpoint đăng ký và đăng nhập với JWT",
  "status": "PENDING",
  "priority": "HIGH",
  "dueDate": "2024-08-20",
  "projectId": 1,
  "assignedTo": 3
}
```

**Phản hồi thành công (201):** Trả về đối tượng công việc vừa tạo.

---

### Cập nhật công việc

**`PUT /api/tasks/{id}`**

**Phản hồi thành công (200):** Trả về đối tượng công việc đã cập nhật.

---

### Xóa công việc

**`DELETE /api/tasks/{id}`**

**Phản hồi thành công (204 No Content)**

---

## 5. 🕐 Chấm công

### Chấm công vào

**`POST /api/attendance/checkin`**

Ghi nhận giờ vào của nhân viên (lấy từ JWT token).

**Phản hồi thành công (200):**
```json
{
  "attendanceId": 101,
  "employeeId": 2,
  "date": "2024-07-15",
  "checkIn": "08:02:35",
  "status": "PRESENT"
}
```

---

### Chấm công ra

**`POST /api/attendance/checkout`**

Cập nhật giờ ra cho bản ghi chấm công trong ngày.

**Phản hồi thành công (200):**
```json
{
  "attendanceId": 101,
  "employeeId": 2,
  "date": "2024-07-15",
  "checkIn": "08:02:35",
  "checkOut": "17:30:10",
  "status": "PRESENT"
}
```

---

### Lấy danh sách chấm công

**`GET /api/attendance`**

Lấy toàn bộ lịch sử chấm công (có thể lọc theo nhân viên hoặc ngày).

**Phản hồi thành công (200):**
```json
[
  {
    "attendanceId": 101,
    "employeeId": 2,
    "date": "2024-07-15",
    "checkIn": "08:02:35",
    "checkOut": "17:30:10",
    "status": "PRESENT"
  }
]
```

---

## 6. 🤖 AI Gợi ý Nhân viên

### Gợi ý nhân viên theo mô tả công việc

**`POST /api/suggestions/recommend`**

Phân tích và gợi ý top 5 nhân viên phù hợp nhất dựa trên mô tả công việc.

**Yêu cầu (Request Body):**
```json
{
  "taskTitle": "Phát triển tính năng thanh toán online",
  "requiredSkills": ["Java", "Spring Boot", "RESTful API"]
}
```

**Phản hồi thành công (200):**
```json
[
  {
    "employeeId": 3,
    "employeeName": "Văn C Lê",
    "score": 0.87,
    "reasoning": "Kỹ năng phù hợp cao (Java, Spring Boot). Khối lượng công việc hiện tại thấp (2/5 task). Hiệu suất hoàn thành đúng hạn 92%. Tỷ lệ chấm công tháng vừa qua 95%."
  }
]
```

---

### Gợi ý nhân viên cho công việc cụ thể

**`GET /api/suggestions/recommend/{taskId}`**

Gợi ý nhân viên dựa trên thông tin công việc đã tồn tại trong hệ thống.

**Phản hồi thành công (200):** Tương tự endpoint trên.

---

### Gửi phản hồi về gợi ý

**`POST /api/suggestions/feedback`**

Ghi nhận phản hồi của quản lý về độ chính xác của gợi ý.

**Yêu cầu (Request Body):**
```json
{
  "suggestionId": 5,
  "feedback": "ACCEPTED",
  "comment": "Nhân viên được gợi ý rất phù hợp với công việc"
}
```

**Phản hồi thành công (200):**
```json
{
  "message": "Đã ghi nhận phản hồi thành công"
}
```
