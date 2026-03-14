# Tài liệu UML — Hệ thống Quản lý Công việc

## Mục lục

- [Mục đích tài liệu](#mục-đích-tài-liệu)
- [1. Sơ đồ Use Case](#1-sơ-đồ-use-case)
- [2. Bảng tóm tắt Use Case](#2-bảng-tóm-tắt-use-case)
- [3. Các Sequence Diagram](#3-các-sequence-diagram)
  - [3.1 Đăng ký tài khoản](#31-đăng-ký-tài-khoản)
  - [3.2 Đăng nhập và cấp JWT](#32-đăng-nhập-và-cấp-jwt)
  - [3.3 Tạo công việc mới](#33-tạo-công-việc-mới)
  - [3.4 Chấm công](#34-chấm-công)
  - [3.5 AI gợi ý nhân viên phù hợp](#35-ai-gợi-ý-nhân-viên-phù-hợp)
  - [3.6 Gửi phản hồi gợi ý](#36-gửi-phản-hồi-gợi-ý)
- [4. Giải thích thuật toán AI Gợi ý Nhân viên](#4-giải-thích-thuật-toán-ai-gợi-ý-nhân-viên)

---

## Mục đích tài liệu

Tài liệu này mô tả kiến trúc và các luồng hoạt động chính của **Hệ thống Quản lý Công việc cho Doanh nghiệp Nhỏ Đa ngành** thông qua các sơ đồ UML được vẽ bằng cú pháp Mermaid (GitHub render trực tiếp).

### Chức năng chính của hệ thống

| Chức năng | Mô tả |
|-----------|-------|
| Quản lý chấm công | Ghi nhận và báo cáo giờ vào/ra của nhân viên |
| Quản lý dự án | Tạo, phân công và theo dõi tiến độ dự án |
| Quản lý công việc | Theo dõi trạng thái, tiến độ và thời hạn hoàn thành |
| Gợi ý nhân viên bằng AI | AI đề xuất nhân viên phù hợp nhất cho từng công việc dựa trên kỹ năng, khối lượng và hiệu suất |

### Công nghệ sử dụng

- **Backend:** Java 25, Spring Boot 3.5.0, Maven
- **Cơ sở dữ liệu:** PostgreSQL
- **Xác thực:** Spring Security + JWT
- **Bộ nhớ đệm:** Redis
- **Frontend:** React 18, Vite, Tailwind CSS, Redux Toolkit
- **Mobile:** Flutter 3.x

---

## 1. Sơ đồ Use Case

```mermaid
graph TD
    subgraph "Hệ thống Quản lý Công việc"
        UC1([Đăng ký tài khoản])
        UC2([Đăng nhập])
        UC3([Xem dự án])
        UC4([Xem công việc])
        UC5([Chấm công])
        UC6([Xem gợi ý AI])
        UC7([Gửi phản hồi gợi ý])
        UC8([Quản lý nhân viên])
        UC9([Quản lý dự án])
        UC10([Quản lý công việc])
        UC11([Xem chấm công])
        UC12([Yêu cầu AI gợi ý nhân viên])
    end

    Khach(["👤 Khách\n(chưa đăng nhập)"])
    NhanVien(["👷 Nhân viên"])
    QuanLy(["👔 Quản lý"])

    Khach --> UC1
    Khach --> UC2

    NhanVien --> UC3
    NhanVien --> UC4
    NhanVien --> UC5
    NhanVien --> UC6
    NhanVien --> UC7

    QuanLy --> UC8
    QuanLy --> UC9
    QuanLy --> UC10
    QuanLy --> UC11
    QuanLy --> UC12
    QuanLy --> UC3
    QuanLy --> UC4
```

---

## 2. Bảng tóm tắt Use Case

| Mã UC | Tên Use Case | Actor | Mô tả ngắn |
|-------|-------------|-------|------------|
| UC01 | Đăng ký tài khoản | Khách | Khách tạo tài khoản mới với tên đăng nhập, email và mật khẩu |
| UC02 | Đăng nhập | Khách | Khách xác thực danh tính và nhận JWT token |
| UC03 | Xem dự án | Nhân viên, Quản lý | Xem danh sách và chi tiết các dự án đang hoạt động |
| UC04 | Xem công việc | Nhân viên, Quản lý | Xem danh sách công việc được giao và trạng thái tiến độ |
| UC05 | Chấm công | Nhân viên | Nhân viên ghi nhận giờ vào/ra làm việc |
| UC06 | Xem gợi ý AI | Nhân viên | Xem danh sách gợi ý nhân viên phù hợp do AI đề xuất |
| UC07 | Gửi phản hồi gợi ý | Nhân viên | Gửi phản hồi (đồng ý/không đồng ý) về gợi ý của AI |
| UC08 | Quản lý nhân viên | Quản lý | Thêm, xem, sửa, xóa thông tin nhân viên (CRUD) |
| UC09 | Quản lý dự án | Quản lý | Thêm, xem, sửa, xóa dự án (CRUD) |
| UC10 | Quản lý công việc | Quản lý | Thêm, xem, sửa, xóa công việc; phân công cho nhân viên (CRUD) |
| UC11 | Xem chấm công | Quản lý | Xem lịch sử chấm công của tất cả nhân viên |
| UC12 | Yêu cầu AI gợi ý nhân viên | Quản lý | Gửi yêu cầu để AI phân tích và đề xuất top 5 nhân viên phù hợp nhất |

---

## 3. Các Sequence Diagram

### 3.1 Đăng ký tài khoản

```mermaid
sequenceDiagram
    participant ND as Người dùng
    participant AC as Bộ điều khiển Auth
    participant US as Dịch vụ Người dùng
    participant DB as Cơ sở dữ liệu

    Note over ND,DB: Luồng đăng ký tài khoản mới

    ND->>AC: POST /api/auth/register<br/>{username, email, password}
    AC->>US: register(registerRequest)

    US->>DB: Kiểm tra username đã tồn tại?
    DB-->>US: Kết quả kiểm tra

    alt Username đã tồn tại
        US-->>AC: Ném ngoại lệ "Tên đăng nhập đã được sử dụng"
        AC-->>ND: 400 Bad Request — Tên đăng nhập đã tồn tại
    else Username chưa tồn tại
        US->>DB: Kiểm tra email đã tồn tại?
        DB-->>US: Kết quả kiểm tra

        alt Email đã tồn tại
            US-->>AC: Ném ngoại lệ "Email đã được sử dụng"
            AC-->>ND: 400 Bad Request — Email đã tồn tại
        else Email hợp lệ
            US->>US: Mã hóa mật khẩu (BCrypt)
            US->>DB: Lưu tài khoản mới
            DB-->>US: Tài khoản đã lưu thành công
            US-->>AC: Tài khoản được tạo
            AC-->>ND: 201 Created — Đăng ký thành công
        end
    end
```

### 3.2 Đăng nhập và cấp JWT

```mermaid
sequenceDiagram
    participant ND as Người dùng
    participant AC as Bộ điều khiển Auth
    participant AM as Trình quản lý xác thực
    participant JP as Nhà cung cấp JWT
    participant DB as Cơ sở dữ liệu

    Note over ND,DB: Luồng đăng nhập và cấp JWT token

    ND->>AC: POST /api/auth/login<br/>{username, password}
    AC->>AM: authenticate(username, password)
    AM->>DB: Tải thông tin người dùng theo username
    DB-->>AM: Thông tin tài khoản + mật khẩu đã mã hóa

    alt Xác thực thất bại
        AM-->>AC: Ngoại lệ xác thực — sai thông tin đăng nhập
        AC-->>ND: 401 Unauthorized — Sai tên đăng nhập hoặc mật khẩu
    else Xác thực thành công
        AM-->>AC: Đối tượng Authentication hợp lệ
        AC->>JP: generateToken(authentication)
        JP->>JP: Tạo JWT với claims<br/>(subject, roles, thời gian hết hạn)
        JP-->>AC: JWT token
        Note over AC: Gói phản hồi: token + thông tin user
        AC-->>ND: 200 OK — {token, tokenType, username, email, roles}
    end
```

### 3.3 Tạo công việc mới

```mermaid
sequenceDiagram
    participant QL as Quản lý
    participant TC as Bộ điều khiển Công việc
    participant TS as Dịch vụ Công việc
    participant DB as Cơ sở dữ liệu
    participant RC as Bộ nhớ đệm Redis

    Note over QL,RC: Luồng tạo công việc mới (yêu cầu JWT hợp lệ)

    QL->>TC: POST /api/tasks<br/>{title, description, dueDate, projectId, assignedToId}
    Note over TC: Xác thực JWT token từ header

    TC->>TS: createTask(taskRequest)
    TS->>DB: Kiểm tra dự án tồn tại (projectId)
    DB-->>TS: Thông tin dự án

    TS->>DB: Kiểm tra nhân viên tồn tại (assignedToId)
    DB-->>TS: Thông tin nhân viên

    TS->>DB: Lưu công việc mới (status: PENDING)
    DB-->>TS: Công việc đã lưu với ID mới

    TS->>RC: Xóa cache "tasks" (CacheEvict)
    RC-->>TS: Cache đã được xóa

    TS-->>TC: Công việc được tạo thành công
    TC-->>QL: 201 Created — {taskId, title, dueDate, status, assignedTo}
```

### 3.4 Chấm công

```mermaid
sequenceDiagram
    participant NV as Nhân viên
    participant ATC as Bộ điều khiển Chấm công
    participant ATS as Dịch vụ Chấm công
    participant DB as Cơ sở dữ liệu
    participant RC as Bộ nhớ đệm Redis

    Note over NV,RC: Luồng ghi nhận chấm công (yêu cầu JWT hợp lệ)

    NV->>ATC: POST /api/attendance<br/>{employeeId, date, checkIn, checkOut}
    Note over ATC: Xác thực JWT token từ header

    ATC->>ATS: logAttendance(attendanceRequest)
    ATS->>DB: Kiểm tra nhân viên tồn tại (employeeId)
    DB-->>ATS: Thông tin nhân viên

    ATS->>DB: Kiểm tra đã chấm công ngày này chưa
    DB-->>ATS: Kết quả kiểm tra

    alt Đã có bản ghi chấm công ngày này
        ATS-->>ATC: Ngoại lệ — Đã chấm công ngày này rồi
        ATC-->>NV: 400 Bad Request — Bản ghi chấm công đã tồn tại
    else Chưa có bản ghi
        ATS->>DB: Lưu bản ghi chấm công mới
        DB-->>ATS: Bản ghi đã lưu thành công

        ATS->>RC: Xóa cache "attendance" (CacheEvict)
        RC-->>ATS: Cache đã được xóa

        ATS-->>ATC: Chấm công thành công
        ATC-->>NV: 201 Created — {attendanceId, employeeId, date, checkIn, checkOut}
    end
```

### 3.5 AI gợi ý nhân viên phù hợp

```mermaid
sequenceDiagram
    participant QL as Quản lý
    participant SC as Bộ điều khiển Gợi ý
    participant AI as Dịch vụ Gợi ý AI
    participant ER as Kho lưu Nhân viên
    participant SR as Kho lưu Kỹ năng
    participant TR as Kho lưu Công việc
    participant AR as Kho lưu Chấm công

    Note over QL,AR: Luồng AI phân tích và đề xuất top 5 nhân viên phù hợp nhất

    QL->>SC: POST /api/suggestions/recommend<br/>{taskId, taskTitle, requiredSkills[]}
    Note over SC: Xác thực JWT — chỉ Quản lý mới được gọi

    SC->>AI: recommendEmployees(taskId, requiredSkills)

    AI->>ER: Lấy toàn bộ danh sách nhân viên
    ER-->>AI: Danh sách tất cả nhân viên

    loop Với mỗi nhân viên trong danh sách
        AI->>SR: Lấy danh sách kỹ năng của nhân viên
        SR-->>AI: Danh sách kỹ năng

        AI->>TR: Đếm công việc đang thực hiện<br/>(PENDING + IN_PROGRESS)
        TR-->>AI: Số lượng công việc hiện tại

        AI->>TR: Lấy công việc đã hoàn thành<br/>để tính hiệu suất
        TR-->>AI: Danh sách công việc đã hoàn thành

        AI->>AR: Lấy bản ghi chấm công<br/>30 ngày gần đây
        AR-->>AI: Số ngày đi làm trong 30 ngày

        Note over AI: Tính điểm từng tiêu chí:
        Note over AI: • Kỹ năng phù hợp (35%)<br/>• Khối lượng công việc (25%)<br/>• Hiệu suất hoàn thành (25%)<br/>• Chấm công (15%)
        AI->>AI: Tính điểm tổng = Kỹ năng×0.35 +<br/>Khối lượng×0.25 + Hiệu suất×0.25 + Chấm công×0.15
    end

    AI->>AI: Sắp xếp nhân viên theo điểm giảm dần
    AI->>AI: Chọn Top 5 nhân viên điểm cao nhất

    AI-->>SC: Danh sách Top 5 nhân viên<br/>(điểm số + lý do gợi ý)
    SC-->>QL: 200 OK — [{employeeId, fullName, totalScore,<br/>skillScore, workloadScore, performanceScore,<br/>attendanceScore, reasoning}]
```

### 3.6 Gửi phản hồi gợi ý

```mermaid
sequenceDiagram
    participant ND as Người dùng
    participant SC as Bộ điều khiển Gợi ý
    participant SS as Dịch vụ Gợi ý
    participant DB as Cơ sở dữ liệu

    Note over ND,DB: Luồng gửi phản hồi về gợi ý của AI

    ND->>SC: POST /api/suggestions/feedback<br/>{suggestionId, feedback, accepted}
    Note over SC: Xác thực JWT token từ header

    SC->>SS: submitFeedback(suggestionId, feedback, accepted)
    SS->>DB: Tìm gợi ý theo suggestionId
    DB-->>SS: Thông tin gợi ý hiện tại

    alt Không tìm thấy gợi ý
        SS-->>SC: Ngoại lệ — Gợi ý không tồn tại
        SC-->>ND: 404 Not Found — Gợi ý không tồn tại
    else Tìm thấy gợi ý
        SS->>DB: Cập nhật nội dung phản hồi và trạng thái chấp nhận
        DB-->>SS: Gợi ý đã được cập nhật

        SS-->>SC: Phản hồi đã được ghi nhận
        SC-->>ND: 200 OK — {suggestionId, feedback, accepted, updatedAt}
    end
```

---

## 4. Giải thích thuật toán AI Gợi ý Nhân viên

### 4.1 Tổng quan

`AiSuggestionService` tự động phân tích và đề xuất **top 5 nhân viên phù hợp nhất** cho một công việc cụ thể. Thuật toán tính điểm tổng hợp từ 4 tiêu chí dựa trên dữ liệu thực tế trong hệ thống.

### 4.2 Công thức tính điểm

```
Điểm tổng = (Điểm kỹ năng × 0.35)
           + (Điểm khối lượng × 0.25)
           + (Điểm hiệu suất × 0.25)
           + (Điểm chấm công × 0.15)
```

### 4.3 Chi tiết 4 tiêu chí

| Tiêu chí | Trọng số | Cách tính | Ý nghĩa |
|----------|----------|-----------|---------|
| **Kỹ năng phù hợp** | 35% | `Số kỹ năng trùng khớp / Tổng số kỹ năng yêu cầu` | Nhân viên có kỹ năng phù hợp với công việc |
| **Khối lượng công việc** | 25% | `1 - (Số task đang làm / 5)` (tối thiểu 0) | Nhân viên ít việc hơn = điểm cao hơn |
| **Hiệu suất** | 25% | `Số task hoàn thành đúng hạn / Tổng task đã hoàn thành` | Tỷ lệ hoàn thành công việc đúng thời hạn |
| **Chấm công** | 15% | `Số ngày đi làm trong 30 ngày gần đây / 22` (tối đa 1.0) | Mức độ chuyên cần và đáng tin cậy |

### 4.4 Ví dụ minh họa

**Tình huống:** Quản lý cần gợi ý nhân viên cho công việc *"Xây dựng giao diện Dashboard"* với kỹ năng yêu cầu: `[React, JavaScript, CSS]`

| Nhân viên | Kỹ năng khớp | Điểm KN (×0.35) | Task đang làm | Điểm KL (×0.25) | Hoàn thành đúng hạn | Điểm HP (×0.25) | Ngày đi làm/30 | Điểm CC (×0.15) | **Điểm tổng** |
|-----------|-------------|-----------------|--------------|-----------------|---------------------|-----------------|----------------|-----------------|--------------|
| Nguyễn Văn A | React, JS, CSS (3/3) | 1.00 × 0.35 = **0.350** | 1 task | 0.80 × 0.25 = **0.200** | 8/10 | 0.80 × 0.25 = **0.200** | 20 ngày | 0.91 × 0.15 = **0.136** | **0.886** |
| Trần Thị B | React, JS (2/3) | 0.67 × 0.35 = **0.233** | 2 task | 0.60 × 0.25 = **0.150** | 9/10 | 0.90 × 0.25 = **0.225** | 22 ngày | 1.00 × 0.15 = **0.150** | **0.758** |
| Lê Văn C | JS, CSS (2/3) | 0.67 × 0.35 = **0.233** | 3 task | 0.40 × 0.25 = **0.100** | 5/10 | 0.50 × 0.25 = **0.125** | 18 ngày | 0.82 × 0.15 = **0.123** | **0.581** |

→ **Kết quả:** AI đề xuất Nguyễn Văn A (0.886 điểm) là ứng viên phù hợp nhất.

### 4.5 Kết quả trả về

```json
[
  {
    "employeeId": 1,
    "fullName": "Nguyễn Văn A",
    "totalScore": 0.886,
    "skillScore": 0.350,
    "workloadScore": 0.200,
    "performanceScore": 0.200,
    "attendanceScore": 0.136,
    "reasoning": "Kỹ năng phù hợp 100%; Đang có 1 task đang thực hiện; Tỷ lệ hoàn thành đúng hạn 80%; Đi làm 20/30 ngày gần đây"
  }
]
```
