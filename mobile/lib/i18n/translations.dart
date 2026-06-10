/// Bảng dịch tiếng Anh. Khóa = chuỗi tiếng Việt gốc (giống quy ước bên frontend).
/// - locale 'vi' → trả về key (Vietnamese gốc)
/// - locale 'en' → tra cứu bảng dưới; không có thì fallback về key
/// - placeholder {name} → thay bằng tham số truyền vào hàm tr().
///
/// Khi cập nhật: thêm cả khóa Việt + bản dịch Anh; đừng tách rời.
const Map<String, String> en = {
  // --- Common navigation / actions ---
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
  'Đăng nhập': 'Log in',
  'Đăng ký': 'Sign up',
  'Đăng ký ngay': 'Sign up now',
  'Huỷ': 'Cancel',
  'Lưu': 'Save',
  'Xoá': 'Delete',
  'Sửa': 'Edit',
  'Tạo': 'Create',
  'Quay lại': 'Back',
  'Đang tải...': 'Loading...',
  'Chưa có dữ liệu': 'No data yet',
  'Có lỗi xảy ra': 'Something went wrong',

  // --- Auth ---
  'Quản lý Công việc': 'Task Manager',
  'Chào mừng trở lại': 'Welcome back',
  'Đăng nhập vào Task Manager': 'Log in to Task Manager',
  'Email': 'Email',
  'Mật khẩu': 'Password',
  'Vui lòng nhập email': 'Please enter your email',
  'Email không hợp lệ': 'Invalid email',
  'Vui lòng nhập mật khẩu': 'Please enter your password',
  'Đăng nhập thất bại': 'Login failed',
  'Chưa có tài khoản?': 'No account yet?',
  'Đã có tài khoản?': 'Already have an account?',

  // --- Register ---
  'Tạo tài khoản': 'Create account',
  'Tham gia hệ thống quản lý công việc': 'Join the task management platform',
  'Tên đăng nhập': 'Username',
  'Vui lòng nhập tên đăng nhập': 'Please enter a username',
  'Tối thiểu 3 ký tự': 'At least 3 characters',
  'Mật khẩu từ 8 đến 100 ký tự': 'Password must be 8–100 characters',
  'Phải có chữ, số và ký tự đặc biệt': 'Must contain a letter, a digit, and a special character',
  'Tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt (vd: !@#\$)':
      'At least 8 characters with letters, digits, and a special character (e.g. !@#\$)',
  'Đăng ký thành công! Vui lòng đăng nhập.': 'Registered successfully! Please log in.',
  'Đăng ký thất bại': 'Registration failed',

  // --- Dashboard / Stats ---
  'Xin chào, {name}': 'Hello, {name}',
  'Xin chào, {name}!': 'Hello, {name}!',
  'Tổng quan': 'Overview',
  'Tổng quan hệ thống': 'System overview',
  'Chấm công hôm nay': 'Attendance today',
  'Chấm công tháng này': 'Attendance this month',
  'Hoàn thành': 'Completed',
  'HOÀN THÀNH': 'COMPLETED',
  'Đang làm': 'In progress',
  'Đang thực hiện': 'In progress',
  'Chờ xử lý': 'Pending',
  'Quá hạn': 'Overdue',
  'Tỷ lệ hoàn thành: {pct}%': 'Completion rate: {pct}%',
  'Phân bố trạng thái công việc': 'Task status breakdown',
  '{total} công việc · {done} đã hoàn thành': '{total} tasks · {done} completed',
  'Đang tải dữ liệu...': 'Loading data...',
  'Đổi ngôn ngữ': 'Change language',
  'bạn': 'you',

  // --- Tasks screen ---
  'Tạo công việc mới': 'Create new task',
  'Tiêu đề *': 'Title *',
  'Bắt buộc': 'Required',
  'Mô tả': 'Description',
  'Hạn hoàn thành (YYYY-MM-DD)': 'Due date (YYYY-MM-DD)',
  'Đã tạo công việc "{title}"': 'Created task "{title}"',
  'Tạo công việc thất bại': 'Failed to create task',
  'Cập nhật trạng thái': 'Update status',
  'Đã cập nhật trạng thái': 'Status updated',
  'Cập nhật thất bại': 'Update failed',
  'Tất cả': 'All',
  'Đang tải danh sách công việc...': 'Loading tasks...',
  'Không có công việc nào': 'No tasks yet',
  'Đổi trạng thái': 'Change status',
  'Thử lại': 'Retry',

  // --- Employees screen ---
  'Tạo nhân viên mới': 'New employee',
  'Thêm nhân viên': 'Add employee',
  'Thêm': 'Add',
  'Họ *': 'Last name *',
  'Tên *': 'First name *',
  'Đã thêm nhân viên {name}': 'Added employee {name}',
  'Thêm thất bại': 'Failed to add',
  'Chưa có nhân viên nào': 'No employees yet',
  'Ngày vào: {date}': 'Hired: {date}',
  'Chức vụ': 'Position',
  'Phòng ban': 'Department',
  'Nhóm': 'Group',
  'Kỹ năng (phân cách bởi dấu phẩy)': 'Skills (comma-separated)',
  'Đã thêm nhân viên': 'Employee added',
  'Thêm nhân viên thất bại': 'Failed to add employee',
  'Đang tải danh sách nhân viên...': 'Loading employees...',
  'Không có nhân viên nào': 'No employees yet',
  'Xoá nhân viên này?': 'Delete this employee?',
  'Đã xoá nhân viên': 'Employee deleted',
  'Xoá thất bại': 'Delete failed',

  // --- Projects screen ---
  'Tạo dự án mới': 'New project',
  'Thêm dự án': 'Add project',
  'Tên dự án *': 'Project name *',
  'Bắt đầu: {date}': 'Start: {date}',
  'Kết thúc: {date}': 'End: {date}',
  'Chưa có dự án nào': 'No projects yet',
  'Đang tải danh sách dự án...': 'Loading projects...',
  'Đã tạo dự án {name}': 'Created project {name}',
  'Ngày bắt đầu (YYYY-MM-DD) *': 'Start date (YYYY-MM-DD) *',
  'Ngày kết thúc (YYYY-MM-DD)': 'End date (YYYY-MM-DD)',
  'Trạng thái': 'Status',
  'Đã tạo dự án': 'Project created',
  'Tạo dự án thất bại': 'Failed to create project',
  'Không có dự án nào': 'No projects yet',
  'Xoá dự án "{name}"?': 'Delete project "{name}"?',
  'Đã xoá dự án': 'Project deleted',
  'Đang triển khai': 'Ongoing',
  'Đã hoàn thành': 'Completed',
  'Đã huỷ': 'Cancelled',

  // --- Attendance screen ---
  'Vị trí GPS': 'GPS location',
  'Đang lấy GPS…': 'Getting GPS…',
  'Cập nhật': 'Refresh',
  'Lịch sử chấm công': 'Attendance history',
  'Chưa có bản ghi hôm nay': 'No records today yet',
  'Chưa có bản ghi': 'No records',
  'Nhân viên #{id}': 'Employee #{id}',
  '{office} – {dist}m (trong vùng)': '{office} – {dist}m (within range)',
  '{office} – {dist}m / {max}m (ngoài vùng, sẽ chờ duyệt)':
      '{office} – {dist}m / {max}m (out of range, will be queued for review)',
  'Chưa có văn phòng nào được cấu hình. Liên hệ quản lý.':
      'No office configured. Contact your manager.',
  'Check-in thành công': 'Check-in succeeded',
  'Check-out thành công': 'Check-out succeeded',
  'Check-in thành công – chờ quản lý duyệt': 'Check-in succeeded — pending review',
  'Check-out thành công – chờ quản lý duyệt': 'Check-out succeeded — pending review',
  'Lỗi: {error}': 'Error: {error}',
  'Vui lòng bật Dịch vụ Định vị (Location).': 'Please enable Location services.',
  'Bạn từ chối quyền truy cập vị trí.': 'Location permission was denied.',
  'Quyền vị trí bị tắt vĩnh viễn. Hãy bật trong cài đặt.':
      'Location permission permanently denied. Please enable it in settings.',
  'Tải xuống Excel': 'Download Excel',
  'Đang tải dữ liệu chấm công...': 'Loading attendance...',
  'Không có dữ liệu chấm công': 'No attendance data',
  'Duyệt chấm công': 'Review attendance',
  'Lý do từ chối (tuỳ chọn)': 'Rejection reason (optional)',
  'Duyệt': 'Approve',
  'Từ chối': 'Reject',
  'Đã duyệt': 'Approved',
  'Đã từ chối': 'Rejected',
  'Chờ duyệt': 'Pending review',
  'Khoảng cách: {m}m': 'Distance: {m}m',
  'Ngoài bán kính cho phép': 'Outside the allowed radius',
  'Trong phạm vi văn phòng': 'Within office radius',
  'Vào: {time}': 'In: {time}',
  'Ra: {time}': 'Out: {time}',

  // --- AI Suggestions screen ---
  'AI Gợi ý nhân viên': 'AI staff suggestions',
  'AI gợi ý nhân viên phù hợp': 'AI suggests the right people',
  'Phân tích kỹ năng, hiệu suất và workload': 'Analyzes skills, performance, and workload',
  'Tiêu đề công việc': 'Task title',
  'Vui lòng nhập tiêu đề': 'Please enter a title',
  'Mô tả (tùy chọn)': 'Description (optional)',
  'Đang phân tích...': 'Analyzing...',
  'Phân tích bằng AI': 'Analyze with AI',
  'AI đang phân tích...': 'AI is analyzing...',
  'AI gợi ý nhân sự': 'AI staff suggestions',
  'Mô tả công việc cần làm': 'Describe the task',
  'Vd: Cần dev React + Spring Boot làm trang quản lý đơn hàng':
      'E.g. Need a React + Spring Boot dev for an order management page',
  'Nhận gợi ý từ AI': 'Get AI suggestion',
  'Đang phân tích bằng AI...': 'Analyzing with AI...',
  'Chưa có gợi ý — nhập yêu cầu rồi nhấn nút bên trên':
      'No suggestions yet — enter a request and tap the button above',
  'Phù hợp {pct}%': '{pct}% match',
  'Lý do': 'Reason',
  'Kỹ năng phù hợp': 'Matching skills',
};

/// Thay placeholder {key} trong [template] bằng giá trị từ [params].
String _interpolate(String template, Map<String, Object?> params) {
  if (params.isEmpty) return template;
  var out = template;
  params.forEach((k, v) {
    out = out.replaceAll('{$k}', '$v');
  });
  return out;
}

/// Trả về bản dịch của [key] theo [locale]. Mặc định fallback về key gốc.
String translate(String locale, String key, [Map<String, Object?> params = const {}]) {
  if (locale == 'vi') return _interpolate(key, params);
  final translated = en[key];
  return _interpolate(translated ?? key, params);
}
