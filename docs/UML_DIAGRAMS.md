# 📊 Sơ đồ UML — Hệ thống Quản lý Công việc

Tài liệu này chứa các sơ đồ Use Case, Class Diagram, Sequence và Activity mô tả kiến trúc và luồng hoạt động chính của hệ thống.

---

## 1. Sơ đồ Use Case

### 1.1. Use Case — Xác thực

```mermaid
graph TB
    subgraph "Hệ thống Xác thực"
        UC1["Đăng ký tài khoản"]
        UC2["Đăng nhập"]
        UC3["Đăng xuất"]
    end
    ND((Người dùng\nchưa đăng nhập)) --> UC1
    ND --> UC2
    DND((Người dùng\nđã đăng nhập)) --> UC3
```

---

### 1.2. Use Case — Quản lý Chấm công

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

### 1.3. Use Case — Quản lý Dự án & Công việc

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

### 1.4. Use Case — AI Gợi ý Nhân viên

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

## 2. Sơ đồ lớp — Class Diagram

### 2.1. Sơ đồ lớp thực thể (Entity)

```mermaid
classDiagram
    class NguoiDung {
        -Long maNguoiDung
        -String tenDangNhap
        -String matKhau
        -String email
        -String vaiTro
        -LocalDateTime ngayTao
        +khởiTạoMặcĐịnh()
    }

    class NhanVien {
        -Long maNhanVien
        -NguoiDung nguoiDung
        -String ho
        -String ten
        -String chucVu
        -String phongBan
        -LocalDateTime ngayVaoLam
        +khởiTạoMặcĐịnh()
    }

    class DuAn {
        -Long maDuAn
        -String tenDuAn
        -String moTa
        -LocalDate ngayBatDau
        -LocalDate ngayKetThuc
        -String trangThai
    }

    class CongViec {
        -Long maCongViec
        -DuAn duAn
        -NhanVien nguoiDuocGiao
        -String tieuDe
        -String moTa
        -LocalDate hanHoanThanh
        -String trangThai
        -LocalDateTime thoiDiemHoanThanh
    }

    class ChamCong {
        -Long maChamCong
        -NhanVien nhanVien
        -LocalDate ngay
        -LocalTime gioVao
        -LocalTime gioRa
    }

    class KyNang {
        -Long maKyNang
        -NhanVien nhanVien
        -String tenKyNang
        -String mucDoThanhThao
    }

    class GoiY {
        -Long maGoiY
        -NguoiDung nguoiDung
        -String noiDungGoiY
        -String phanHoi
        -LocalDateTime ngayTao
        +khởiTạoMặcĐịnh()
    }

    NhanVien "*" --> "1" NguoiDung : liên kết tài khoản
    CongViec "*" --> "1" DuAn : thuộc dự án
    CongViec "*" --> "0..1" NhanVien : được phân công
    ChamCong "*" --> "1" NhanVien : ghi nhận chấm công
    KyNang "*" --> "1" NhanVien : sở hữu
    GoiY "*" --> "1" NguoiDung : người tạo
```

### 2.2. Sơ đồ lớp kiến trúc (Controller — Service — Repository)

```mermaid
classDiagram
    class BoDieuKhienXacThuc
    class BoDieuKhienNhanVien
    class BoDieuKhienDuAn
    class BoDieuKhienCongViec
    class BoDieuKhienChamCong
    class BoDieuKhienGoiY

    class DichVuNguoiDung
    class DichVuNhanVien
    class DichVuDuAn
    class DichVuCongViec
    class DichVuChamCong
    class DichVuGoiY
    class DichVuGoiYAI

    class KhoLuuNguoiDung
    class KhoLuuNhanVien
    class KhoLuuDuAn
    class KhoLuuCongViec
    class KhoLuuChamCong
    class KhoLuuKyNang
    class KhoLuuGoiY

    BoDieuKhienXacThuc --> DichVuNguoiDung
    BoDieuKhienNhanVien --> DichVuNhanVien
    BoDieuKhienDuAn --> DichVuDuAn
    BoDieuKhienCongViec --> DichVuCongViec
    BoDieuKhienChamCong --> DichVuChamCong
    BoDieuKhienGoiY --> DichVuGoiY
    BoDieuKhienGoiY --> DichVuGoiYAI

    DichVuNguoiDung --> KhoLuuNguoiDung
    DichVuNhanVien --> KhoLuuNhanVien
    DichVuDuAn --> KhoLuuDuAn
    DichVuCongViec --> KhoLuuCongViec
    DichVuChamCong --> KhoLuuChamCong
    DichVuGoiY --> KhoLuuGoiY
    DichVuGoiYAI --> KhoLuuNhanVien
    DichVuGoiYAI --> KhoLuuKyNang
    DichVuGoiYAI --> KhoLuuCongViec
    DichVuGoiYAI --> KhoLuuChamCong

    KhoLuuNguoiDung --> NguoiDung
    KhoLuuNhanVien --> NhanVien
    KhoLuuDuAn --> DuAn
    KhoLuuCongViec --> CongViec
    KhoLuuChamCong --> ChamCong
    KhoLuuKyNang --> KyNang
    KhoLuuGoiY --> GoiY
```

---

## 3. Sơ đồ tuần tự — Sequence Diagram

### 3.1. Đăng nhập

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
        Note over BE: So sánh mật khẩu với BCrypt
        BE-->>FE: 200 OK + JWT Token + role
        FE-->>ND: Chuyển hướng đến Bảng điều khiển
    else Xác thực thất bại
        BE-->>FE: 401 Không được phép
        FE-->>ND: Hiển thị thông báo lỗi
    end
```

---

### 3.2. Chấm công

```mermaid
sequenceDiagram
    participant NV as Nhân viên
    participant FE as Giao diện
    participant BE as Máy chủ
    participant DB as Cơ sở dữ liệu

    NV->>FE: Nhấn nút "Chấm công vào"
    FE->>BE: POST /api/attendance/checkin
    Note over BE: Xác thực JWT
    BE->>DB: Kiểm tra đã chấm công hôm nay chưa
    DB-->>BE: Kết quả kiểm tra
    alt Chưa chấm công
        BE->>DB: Lưu bản ghi chấm công (ngày, giờ vào)
        DB-->>BE: Xác nhận đã lưu
        BE-->>FE: 200 OK
        FE-->>NV: Hiển thị "Đã chấm công vào"
    else Đã chấm công rồi
        BE-->>FE: 400 Bad Request
        FE-->>NV: Hiển thị lỗi "Đã chấm công rồi"
    end

    NV->>FE: Nhấn nút "Chấm công ra"
    FE->>BE: POST /api/attendance/checkout
    Note over BE: Xác thực JWT
    BE->>DB: Cập nhật giờ ra
    DB-->>BE: Xác nhận
    BE-->>FE: 200 OK
    FE-->>NV: Hiển thị "Đã chấm công ra"
```

---

### 3.3. AI Gợi ý Nhân viên

```mermaid
sequenceDiagram
    participant QL as Quản lý
    participant BDK as SuggestionController
    participant AI as AiSuggestionService
    participant EmpRepo as EmployeeRepository
    participant SkillRepo as SkillRepository
    participant TaskRepo as TaskRepository
    participant AttRepo as AttendanceRepository
    participant OpenAI as OpenAI API

    Note over QL,OpenAI: Luồng AI phân tích và đề xuất top 5 nhân viên phù hợp nhất

    QL->>BDK: POST /api/suggestions/recommend
    Note over BDK: Xác thực JWT — chỉ Quản lý mới được gọi

    BDK->>AI: recommendEmployees(request)

    AI->>EmpRepo: findAll()
    EmpRepo-->>AI: List~Employee~

    AI->>SkillRepo: findByEmployeeEmployeeIdIn(ids)
    SkillRepo-->>AI: List~Skill~ (batch — groupingBy employeeId)

    AI->>TaskRepo: findByAssignedToEmployeeIdInAndStatusIn(ids, statuses)
    TaskRepo-->>AI: List~Task~ (batch — groupingBy employeeId)

    AI->>AttRepo: findByEmployeeEmployeeIdInAndDateBetween(ids, start, end)
    AttRepo-->>AI: List~Attendance~ (batch — groupingBy employeeId)

    alt OpenAI API Key có cấu hình
        AI->>AI: buildPrompt(request, employees, skills, tasks, attendance)
        AI->>OpenAI: POST /v1/chat/completions
        OpenAI-->>AI: JSON response
        AI->>AI: parseOpenAiResponse(responseJson, employees)
    else Không có API Key (Fallback)
        AI->>AI: calculateFallbackScores(...)
        Note over AI: Tính điểm rule-based: Skill 35% + Workload 25% + Performance 25% + Attendance 15%
    end

    AI-->>BDK: Top 5 EmployeeSuggestionDTO (sắp xếp theo overallScore giảm dần)
    BDK-->>QL: 200 OK + Danh sách gợi ý kèm lý do
```

---

### 3.4. Quản lý Công việc (Tạo + Phân công)

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

---

## 4. Sơ đồ hoạt động — Activity Diagram

### 4.1. Đăng nhập

```mermaid
flowchart TD
    A([Bắt đầu]) --> B[Nhập username + password]
    B --> C{Username tồn tại?}
    C -- Không --> D[Hiển thị lỗi: Sai thông tin đăng nhập]
    D --> B
    C -- Có --> E{Password khớp BCrypt?}
    E -- Không --> D
    E -- Có --> F[Tạo JWT Token]
    F --> G[Trả về token + role]
    G --> H[Chuyển đến Dashboard]
    H --> I([Kết thúc])
```

---

### 4.2. Đăng ký

```mermaid
flowchart TD
    A([Bắt đầu]) --> B[Nhập username, email, password]
    B --> C{Username đã tồn tại?}
    C -- Có --> D[Hiển thị lỗi: Username đã tồn tại]
    D --> B
    C -- Không --> E{Email đã tồn tại?}
    E -- Có --> F[Hiển thị lỗi: Email đã tồn tại]
    F --> B
    E -- Không --> G[Mã hóa password BCrypt]
    G --> H[Tạo User mới với role = EMPLOYEE]
    H --> I[Lưu vào database]
    I --> J[Trả về thông tin user]
    J --> K([Kết thúc])
```

---

### 4.3. Quản lý Nhân viên

```mermaid
flowchart TD
    A([Bắt đầu]) --> B{Chọn thao tác?}
    B -- Xem --> C[GET /api/employees]
    C --> D[Hiển thị danh sách nhân viên]
    B -- Thêm --> E[Nhập thông tin nhân viên]
    E --> F[POST /api/employees]
    F --> G{Dữ liệu hợp lệ?}
    G -- Không --> H[Hiển thị lỗi validation]
    H --> E
    G -- Có --> I[Lưu nhân viên mới]
    I --> D
    B -- Sửa --> J[Chọn nhân viên cần sửa]
    J --> K[PUT /api/employees/{id}]
    K --> D
    B -- Xóa --> L[Chọn nhân viên cần xóa]
    L --> M[DELETE /api/employees/{id}]
    M --> D
    D --> N([Kết thúc])
```

---

### 4.4. Chấm công

```mermaid
flowchart TD
    A([Bắt đầu]) --> B{Loại chấm công?}
    B -- Chấm công vào --> C[POST /api/attendance/checkin]
    C --> D{Đã chấm công hôm nay?}
    D -- Có --> E[Hiển thị lỗi: Đã chấm công rồi]
    D -- Không --> F[Tạo record mới: date + checkIn time]
    F --> G[Lưu vào DB]
    G --> H[Hiển thị: Đã chấm công vào]
    B -- Chấm công ra --> I[POST /api/attendance/checkout]
    I --> J{Có record chấm công vào?}
    J -- Không --> K[Hiển thị lỗi: Chưa chấm công vào]
    J -- Có --> L[Cập nhật checkOut time]
    L --> M[Hiển thị: Đã chấm công ra]
    B -- Xem lịch sử --> N[GET /api/attendance]
    N --> O[Hiển thị lịch sử chấm công]
    H --> P([Kết thúc])
    M --> P
    O --> P
    E --> P
    K --> P
```

---

### 4.5. AI Gợi ý Nhân viên

```mermaid
flowchart TD
    A([Bắt đầu]) --> B[Nhập: taskTitle, taskDescription, requiredSkills]
    B --> C[POST /api/suggestions/recommend]
    C --> D[findAll employees — batch query]
    D --> E[findByEmployeeEmployeeIdIn — batch skills]
    E --> F[findByAssignedToEmployeeIdInAndStatusIn — batch active tasks]
    F --> G[findByEmployeeEmployeeIdInAndDateBetween — batch attendance 30 ngày]
    G --> H{OpenAI API Key có cấu hình?}
    H -- Có --> I[buildPrompt với dữ liệu thực tế]
    I --> J[POST /v1/chat/completions tới OpenAI GPT]
    J --> K[parseOpenAiResponse]
    K --> L[Top 5 EmployeeSuggestionDTO]
    H -- Không --> M[calculateFallbackScores]
    M --> N[Skill Match 35%]
    M --> O[Workload 25%]
    M --> P[Performance 25%]
    M --> Q[Attendance 15%]
    N --> R[Tính overallScore tổng hợp]
    O --> R
    P --> R
    Q --> R
    R --> S[Sắp xếp giảm dần theo overallScore]
    S --> L
    L --> T[Hiển thị kết quả gợi ý + lý do]
    T --> U([Kết thúc])
```

---

### 4.6. Quản lý Dự án

```mermaid
flowchart TD
    A([Bắt đầu]) --> B{Chọn thao tác?}
    B -- Xem --> C[GET /api/projects]
    C --> D[Hiển thị danh sách dự án]
    B -- Tạo mới --> E[Nhập: name, description, startDate, endDate]
    E --> F[POST /api/projects]
    F --> G[Lưu dự án mới, status = ongoing]
    G --> D
    B -- Cập nhật --> H[Chọn dự án cần sửa]
    H --> I[PUT /api/projects/{id}]
    I --> D
    B -- Xóa --> J[DELETE /api/projects/{id}]
    J --> D
    D --> K([Kết thúc])
```

---

### 4.7. Quản lý Công việc

```mermaid
flowchart TD
    A([Bắt đầu]) --> B{Chọn thao tác?}
    B -- Xem --> C[GET /api/tasks]
    C --> D[Hiển thị danh sách công việc]
    B -- Tạo --> E[Nhập: title, description, dueDate, projectId]
    E --> F[POST /api/tasks]
    F --> G[Tạo task, status = pending]
    G --> D
    B -- Phân công --> H[Chọn task + chọn nhân viên]
    H --> I[PUT /api/tasks/{id} với assignedTo]
    I --> D
    B -- Cập nhật trạng thái --> J[Chọn task]
    J --> K{Status mới?}
    K -- COMPLETED --> L[Cập nhật completedAt = now]
    K -- IN_PROGRESS --> M[Cập nhật status]
    L --> D
    M --> D
    B -- Xóa --> N[DELETE /api/tasks/{id}]
    N --> D
    D --> O([Kết thúc])
```
