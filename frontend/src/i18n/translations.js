// Bản dịch tiếng Anh. Khóa (key) chính là chuỗi tiếng Việt gốc:
// - lang === 'vi'  → trả về chính khóa
// - lang === 'en'  → tra cứu trong bảng dưới, không có thì fallback về khóa
// Placeholder dạng {name} được thay ở hàm t().
export const en = {
  // --- Điều hướng / Sidebar ---
  'Bảng điều khiển': 'Dashboard',
  'Nhân viên': 'Employees',
  'Dự án': 'Projects',
  'Công việc': 'Tasks',
  'Chấm công': 'Attendance',
  'Văn phòng': 'Offices',
  'AI Gợi ý': 'AI Suggestions',
  'Công việc của tôi': 'My Tasks',
  'Chấm công của tôi': 'My Attendance',
  'Người dùng': 'Users',
  'Đăng xuất': 'Log out',
  'Quản lý': 'Manager',
  'Quản trị viên': 'Administrator',

  // --- Đăng nhập ---
  'Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.':
    'Login failed. Please check your email and password.',
  'Chào mừng trở lại': 'Welcome back',
  'Đăng nhập vào Task Manager': 'Sign in to Task Manager',
  'Mật khẩu': 'Password',
  'Đang đăng nhập...': 'Signing in...',
  'Đăng nhập': 'Sign in',
  'Chưa có tài khoản?': "Don't have an account?",
  'Đăng ký ngay': 'Sign up now',

  // --- Đăng ký ---
  'Mật khẩu xác nhận không khớp.': 'Password confirmation does not match.',
  'Đăng ký thành công! Đang chuyển đến trang đăng nhập...':
    'Registration successful! Redirecting to the login page...',
  'Đăng ký thất bại. Vui lòng thử lại.': 'Registration failed. Please try again.',
  'Tên người dùng': 'Username',
  'Xác nhận mật khẩu': 'Confirm password',
  'Tạo tài khoản': 'Create account',
  'Tham gia Task Manager hôm nay': 'Join Task Manager today',
  'Đang đăng ký...': 'Signing up...',
  'Đăng ký': 'Sign up',
  'Đã có tài khoản?': 'Already have an account?',

  // --- Dashboard ---
  'Tổng quan hệ thống': 'System overview',
  '{tasks} công việc đã được tạo, {done} đã hoàn thành':
    '{tasks} tasks created, {done} completed',
  'Tỷ lệ hoàn thành': 'Completion rate',
  'Chấm công hôm nay': "Today's attendance",
  'Phân bổ trạng thái công việc': 'Task status distribution',
  'Chưa có công việc': 'No tasks yet',
  'Nhân viên theo phòng ban': 'Employees by department',
  'Chưa có dữ liệu nhân viên': 'No employee data yet',
  'Số nhân viên': 'Number of employees',
  'Khác': 'Other',
  'Xin chào, {name}!': 'Hello, {name}!',
  'Bạn có {tasks} công việc được giao, {done} đã hoàn thành.':
    'You have {tasks} assigned tasks, {done} completed.',
  'Chờ xử lý': 'Pending',
  'Đang thực hiện': 'In progress',
  'Đã hoàn thành': 'Completed',
  'Chấm công tháng này': "This month's attendance",
  'Công việc của tôi theo trạng thái': 'My tasks by status',
  'Bạn chưa có công việc nào': "You don't have any tasks",
  'Công việc sắp đến hạn': 'Upcoming deadlines',
  'Không có công việc nào sắp đến hạn': 'No upcoming deadlines',

  // --- Trạng thái dùng chung ---
  'Hoàn thành': 'Completed',
  'Hoạt động': 'Active',
  'Đúng tiến độ': 'On track',
  'Quá hạn': 'Overdue',

  // --- Hành động dùng chung ---
  'Hành động': 'Actions',
  'Sửa': 'Edit',
  'Xóa': 'Delete',
  'Hủy': 'Cancel',
  'Lưu': 'Save',
  'Đang lưu...': 'Saving...',
  'Xóa thất bại.': 'Delete failed.',
  'Lưu thất bại.': 'Save failed.',
  'Mô tả': 'Description',
  'Trạng thái': 'Status',
  'Lỗi:': 'Error:',

  // --- Nhân viên ---
  'Bạn có chắc muốn xóa nhân viên này?': 'Are you sure you want to delete this employee?',
  'Thêm nhân viên': 'Add employee',
  'Tìm kiếm theo tên, phòng ban...': 'Search by name or department...',
  'Họ': 'First name',
  'Tên': 'Last name',
  'Chức vụ': 'Position',
  'Phòng ban': 'Department',
  'Không có nhân viên nào.': 'No employees found.',
  'Chỉnh sửa nhân viên': 'Edit employee',
  'Thêm nhân viên mới': 'Add new employee',
  'VD: Kỹ sư phần mềm': 'e.g. Software Engineer',
  'VD: IT, Kế toán, Nhân sự': 'e.g. IT, Accounting, HR',
  'Kỹ năng': 'Skills',
  'VD: Java, Spring Boot, PostgreSQL, React': 'e.g. Java, Spring Boot, PostgreSQL, React',
  'Quản lý nhập tự do, ngăn cách bởi dấu phẩy. AI sẽ dùng để đối chiếu khi gợi ý.':
    'Free text, comma-separated. AI uses this for matching when making suggestions.',

  // --- Dự án ---
  'Bạn có chắc muốn xóa dự án này?': 'Are you sure you want to delete this project?',
  'Thêm dự án': 'Add project',
  'Tên dự án': 'Project name',
  'Ngày bắt đầu': 'Start date',
  'Ngày kết thúc': 'End date',
  'Chưa có dự án nào.': 'No projects yet.',
  'Chỉnh sửa dự án': 'Edit project',
  'Thêm dự án mới': 'Add new project',
  'Nhập tên dự án': 'Enter the project name',
  'Mô tả ngắn về dự án': 'Short project description',

  // --- Công việc ---
  'Bạn có chắc muốn xóa công việc này?': 'Are you sure you want to delete this task?',
  'Thêm công việc': 'Add task',
  'Tiêu đề': 'Title',
  'Hạn chót': 'Due date',
  'Phân công': 'Assignee',
  'Chưa có công việc nào.': 'No tasks yet.',
  'Chỉnh sửa công việc': 'Edit task',
  'Thêm công việc mới': 'Add new task',
  'Nhập tiêu đề công việc': 'Enter the task title',
  'Mô tả công việc': 'Task description',
  'Kỹ năng yêu cầu': 'Required skills',
  'VD: Java, Spring Boot, PostgreSQL': 'e.g. Java, Spring Boot, PostgreSQL',
  'Quản lý nhập tự do, AI sẽ đối chiếu với kỹ năng của nhân viên khi gợi ý.':
    'Free text; AI matches it against employee skills when making suggestions.',
  'Phân công cho': 'Assign to',
  '-- Chọn dự án --': '-- Select a project --',
  '-- Chọn nhân viên --': '-- Select an employee --',

  // --- Chấm công ---
  'Đã duyệt': 'Approved',
  'Chờ duyệt': 'Pending review',
  'Từ chối': 'Rejected',
  'Lọc theo nhân viên:': 'Filter by employee:',
  'Tất cả nhân viên': 'All employees',
  'Ngày': 'Date',
  'Giờ vào': 'Check-in',
  'Giờ ra': 'Check-out',
  'Khoảng cách': 'Distance',
  'Chưa có dữ liệu chấm công.': 'No attendance records yet.',
  'Duyệt': 'Approve',
  'Chấm công thất bại.': 'Failed to log attendance.',
  'Ghi nhận chấm công': 'Log attendance',

  // --- Công việc của tôi ---
  'Các nhiệm vụ được quản lý phân cho bạn. Bạn có thể cập nhật trạng thái công việc trực tiếp.':
    'Tasks assigned to you by your manager. You can update task status directly.',
  'Cập nhật': 'Update',
  'Bạn chưa được giao công việc nào.': 'You have not been assigned any tasks.',
  'Không tải được danh sách công việc của bạn.': 'Failed to load your task list.',
  'Cập nhật trạng thái thất bại.': 'Failed to update status.',

  // --- Chấm công của tôi ---
  'Hệ thống dùng GPS để xác minh vị trí. Nếu nằm ngoài vùng cho phép, bản ghi sẽ chuyển sang':
    'The system uses GPS to verify your location. If you are outside the allowed area, the record changes to',
  'trạng thái chờ quản lý duyệt': 'a status awaiting manager approval',
  'Vị trí hiện tại': 'Current location',
  'Đang lấy GPS…': 'Getting GPS…',
  'Gần nhất:': 'Nearest:',
  'Khoảng cách:': 'Distance:',
  'trong vùng': 'inside the area',
  'ngoài vùng cho phép': 'outside the allowed area',
  'Chưa có văn phòng nào được cấu hình. Liên hệ quản lý.':
    'No office has been configured. Contact your manager.',
  'Vào ca (Check-in)': 'Check in',
  'Tan ca (Check-out)': 'Check out',
  'Chưa có bản ghi chấm công nào.': 'No attendance records yet.',
  'Không tải được dữ liệu.': 'Failed to load data.',
  'Trình duyệt không hỗ trợ Geolocation API.': 'This browser does not support the Geolocation API.',
  'Không lấy được vị trí. Hãy cấp quyền GPS cho trình duyệt.':
    'Could not get your location. Please grant GPS permission to the browser.',
  'Chấm công vào': 'Check-in',
  'Chấm công ra': 'Check-out',
  '{label} thành công.': '{label} successful.',
  '{label} thành công nhưng vị trí ngoài vùng cho phép – đã chuyển sang chờ duyệt.':
    '{label} successful, but the location is outside the allowed area – moved to pending review.',
  '{label} thất bại.': '{label} failed.',

  // --- AI Gợi ý ---
  'AI Gợi ý nhân viên': 'AI employee suggestions',
  'Phân tích lịch sử và đề xuất người phù hợp nhất':
    'Analyzes history and recommends the best-fit people',
  'Tiêu đề công việc': 'Task title',
  'tùy chọn': 'optional',
  'Đang phân tích...': 'Analyzing...',
  'Phân tích bằng AI': 'Analyze with AI',
  'Không thể lấy gợi ý.': 'Could not get suggestions.',
  'Không tìm thấy nhân viên phù hợp': 'No suitable employee found',
  'Hãy thử mô tả task chi tiết hơn': 'Try describing the task in more detail',

  // --- Văn phòng & Geofence ---
  'Văn phòng & Geofence': 'Offices & Geofence',
  'Cấu hình điểm cho phép chấm công bằng GPS. Bán kính tính bằng mét.':
    'Configure points where GPS attendance is allowed. Radius is in meters.',
  'Thêm văn phòng': 'Add office',
  'Tên văn phòng': 'Name',
  'Địa chỉ': 'Address',
  'Toạ độ': 'Coordinates',
  'Bán kính': 'Radius',
  'Chưa có văn phòng nào.': 'No offices yet.',
  'Sửa văn phòng': 'Edit office',
  'Thêm văn phòng': 'Add office',
  'Tên *': 'Name *',
  'Bán kính (m)': 'Radius (m)',
  'Dùng vị trí hiện tại': 'Use current location',
  'Không tải được danh sách văn phòng.': 'Failed to load the office list.',
  'Xoá văn phòng "{name}"?': 'Delete office "{name}"?',
  'Xoá thất bại.': 'Delete failed.',
  'Bán kính: {radius}m': 'Radius: {radius}m',
  'Vị trí của bạn': 'Your location',

  // --- Người dùng & Phân quyền ---
  'Người dùng & Phân quyền': 'Users & Permissions',
  'Tài khoản mới đăng ký mặc định là Nhân viên. Quản trị viên đổi vai trò để cấp quyền quản lý.':
    'Newly registered accounts default to Employee. Administrators change roles to grant management rights.',
  'Tên đăng nhập': 'Username',
  'Vai trò hiện tại': 'Current role',
  'Phân quyền': 'Assign role',
  'Chưa có người dùng nào.': 'No users yet.',
  'Không thể đổi': 'Cannot change',
  'Đã đổi vai trò của "{name}" thành {role}.': 'Changed the role of "{name}" to {role}.',
  'Đổi vai trò thất bại.': 'Failed to change role.',

  // --- Modal / ErrorBoundary ---
  'Đã xảy ra lỗi': 'An error occurred',
  'Ứng dụng gặp sự cố không mong muốn. Vui lòng tải lại trang.':
    'The application ran into an unexpected problem. Please reload the page.',
  'Tải lại trang': 'Reload page',

  // --- Trang Landing & chế độ dùng thử ---
  'Quản lý công việc thông minh cho doanh nghiệp nhỏ':
    'Smart task management for small businesses',
  'Giao việc, theo dõi tiến độ, chấm công GPS và để AI gợi ý nhân viên phù hợp — tất cả trong một hệ thống.':
    'Assign work, track progress, GPS attendance, and let AI suggest the right people — all in one system.',
  'Dùng thử ngay': 'Try it now',
  'Chế độ dùng thử cho xem trước giao diện với dữ liệu mẫu — không cần tài khoản.':
    'Demo mode previews the interface with sample data — no account needed.',
  'Tính năng nổi bật': 'Key features',
  'Sẵn sàng trải nghiệm hệ thống?': 'Ready to explore the system?',
  'Đăng nhập để dùng đầy đủ, hoặc dùng thử ngay với dữ liệu mẫu.':
    'Sign in for full access, or try it now with sample data.',
  'Quản lý công việc & dự án': 'Task & project management',
  'Giao việc, theo dõi tiến độ, hạn chót và trạng thái.':
    'Assign work; track progress, deadlines and status.',
  'AI gợi ý nhân viên': 'AI employee suggestions',
  'Google Gemini phân tích và đề xuất người phù hợp nhất.':
    'Google Gemini analyzes and recommends the best-fit people.',
  'Chấm công GPS': 'GPS attendance',
  'Xác thực vị trí bằng bản đồ, hạn chế chấm công gian lận.':
    'Verify location on a map to reduce attendance fraud.',
  'Phân quyền & bảo mật': 'Roles & security',
  'Ba vai trò, xác thực JWT, dữ liệu được bảo vệ nhiều lớp.':
    'Three roles, JWT authentication, multi-layer data protection.',
  'Dashboard trực quan': 'Visual dashboard',
  'Biểu đồ thống kê công việc và nhân sự theo thời gian thực.':
    'Real-time charts of tasks and staff.',
  'Song ngữ Việt / Anh': 'Vietnamese / English',
  'Chuyển đổi ngôn ngữ giao diện tức thì, mọi lúc.':
    'Switch the interface language instantly, anytime.',
  'Chế độ dùng thử — bạn đang xem dữ liệu mẫu.':
    'Demo mode — you are viewing sample data.',
  'Đăng nhập để dùng đầy đủ': 'Sign in for full access',
  'Khách dùng thử': 'Demo guest',
  'Chế độ dùng thử': 'Demo mode',
  'Thoát dùng thử': 'Exit demo',
}
