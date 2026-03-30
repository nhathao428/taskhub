# 📊 Sơ đồ UML — Hệ thống Quản lý Công việc

Tài liệu này chứa các sơ đồ Use Case và Sequence mô tả luồng hoạt động chính của hệ thống.

---

## 1. Sơ đồ Use Case — Quản lý Chấm công

```mermaid
graph TB
    subgraph "Hệ thống Chấm công"
        UC1["Chấm công vào"]
        UC2["Chấm công ra"]
        UC3["Xem lịch sử chấm công"]
        UC4["Báo cáo chấm công"]
    end
    NV((Nhân viên)) --> UC1
    NV --> UC2
    NV --> UC3
    QL((Quản lý)) --> UC3
    QL --> UC4
```

---

## 2. Sơ đồ Use Case — Quản lý Dự án & Công việc

```mermaid
graph TB
    subgraph "Hệ thống Quản lý Dự án"
        UC1["Tạo dự án"]
        UC2["Chỉnh sửa dự án"]
        UC3["Xóa dự án"]
        UC4["Tạo công việc"]
        UC5["Phân công công việc"]
        UC6["Cập nhật tiến độ"]
        UC7["Xem công việc"]
    end
    QL((Quản lý)) --> UC1
    QL --> UC2
    QL --> UC3
    QL --> UC4
    QL --> UC5
    NV((Nhân viên)) --> UC6
    NV --> UC7
    QL --> UC7
```

---

## 3. Sơ đồ Use Case — AI Gợi ý Nhân viên

```mermaid
graph TB
    subgraph "Hệ thống AI Gợi ý"
        UC1["Yêu cầu gợi ý nhân viên"]
        UC2["Phân tích kỹ năng"]
        UC3["Đánh giá khối lượng công việc"]
        UC4["Tính điểm hiệu suất"]
        UC5["Kiểm tra chấm công"]
        UC6["Trả về top 5 nhân viên"]
        UC7["Gửi phản hồi"]
    end
    QL((Quản lý)) --> UC1
    UC1 --> UC2
    UC1 --> UC3
    UC1 --> UC4
    UC1 --> UC5
    UC2 --> UC6
    UC3 --> UC6
    UC4 --> UC6
    UC5 --> UC6
    QL --> UC7
```

---

## 4. Sơ đồ Sequence — Đăng nhập

```mermaid
sequenceDiagram
    participant ND as Người dùng
    participant FE as Giao diện
    participant BE as Máy chủ
    participant DB as Cơ sở dữ liệu

    ND->>FE: Nhập tên đăng nhập + mật khẩu
    FE->>BE: POST /api/auth/login
    BE->>DB: Truy vấn thông tin người dùng
    DB-->>BE: Trả về dữ liệu người dùng
    alt Xác thực thành công
        BE-->>FE: 200 OK + JWT Token
        FE-->>ND: Chuyển hướng đến Bảng điều khiển
    else Xác thực thất bại
        BE-->>FE: 401 Không được phép
        FE-->>ND: Hiển thị thông báo lỗi
    end
```

---

## 5. Sơ đồ Sequence — Chấm công

```mermaid
sequenceDiagram
    participant NV as Nhân viên
    participant FE as Giao diện
    participant BE as Máy chủ
    participant DB as Cơ sở dữ liệu

    NV->>FE: Nhấn nút "Chấm công vào"
    FE->>BE: POST /api/attendance/checkin
    Note over BE: Xác thực JWT
    BE->>DB: Lưu bản ghi chấm công (ngày, giờ vào)
    DB-->>BE: Xác nhận đã lưu
    BE-->>FE: 200 OK
    FE-->>NV: Hiển thị "Đã chấm công vào"

    NV->>FE: Nhấn nút "Chấm công ra"
    FE->>BE: POST /api/attendance/checkout
    BE->>DB: Cập nhật giờ ra
    DB-->>BE: Xác nhận
    BE-->>FE: 200 OK
    FE-->>NV: Hiển thị "Đã chấm công ra"
```

---

## 6. Sơ đồ Sequence — AI Gợi ý Nhân viên

```mermaid
sequenceDiagram
    participant QL as Quản lý
    participant BDK as Bộ điều khiển Gợi ý
    participant AI as Dịch vụ Gợi ý AI
    participant KLN as Kho lưu Nhân viên
    participant KLK as Kho lưu Kỹ năng
    participant KLCV as Kho lưu Công việc
    participant KLCC as Kho lưu Chấm công

    Note over QL,KLCC: Luồng AI phân tích và đề xuất top 5 nhân viên phù hợp nhất

    QL->>BDK: POST /api/suggestions/recommend
    Note over BDK: Xác thực JWT — chỉ Quản lý mới được gọi

    BDK->>AI: gợiÝNhânViên(tiêu đề, kỹ năng yêu cầu)

    AI->>KLN: Lấy toàn bộ danh sách nhân viên
    KLN-->>AI: Danh sách tất cả nhân viên

    loop Với mỗi nhân viên trong danh sách
        AI->>KLK: Lấy danh sách kỹ năng của nhân viên
        KLK-->>AI: Danh sách kỹ năng

        AI->>KLCV: Đếm công việc đang thực hiện
        KLCV-->>AI: Số lượng công việc hiện tại

        AI->>KLCV: Lấy các công việc đã hoàn thành
        KLCV-->>AI: Danh sách công việc đã hoàn thành

        AI->>KLCC: Lấy bản ghi chấm công 30 ngày gần nhất
        KLCC-->>AI: Danh sách chấm công

        Note over AI: Tính điểm tổng hợp: Kỹ năng (35%) + Khối lượng (25%) + Hiệu suất (25%) + Chấm công (15%)
    end

    AI-->>BDK: Top 5 nhân viên phù hợp nhất (sắp xếp theo điểm giảm dần)
    BDK-->>QL: 200 OK + Danh sách gợi ý kèm lý do
```

---

## 7. Sơ đồ Sequence — Quản lý Công việc (Tạo + Phân công)

```mermaid
sequenceDiagram
    participant QL as Quản lý
    participant FE as Giao diện
    participant BE as Máy chủ
    participant DB as Cơ sở dữ liệu

    QL->>FE: Điền thông tin công việc mới
    FE->>BE: POST /api/tasks
    Note over BE: Xác thực JWT
    BE->>DB: Lưu công việc mới
    DB-->>BE: Xác nhận đã tạo
    BE-->>FE: 201 Đã tạo
    FE-->>QL: Hiển thị công việc mới trong danh sách

    QL->>FE: Phân công nhân viên cho công việc
    FE->>BE: PUT /api/tasks/{id}
    BE->>DB: Cập nhật assigned_to
    DB-->>BE: Xác nhận
    BE-->>FE: 200 OK
    FE-->>QL: Cập nhật giao diện
```
