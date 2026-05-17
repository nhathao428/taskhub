// Dữ liệu mẫu cho chế độ "dùng thử" (demo) — khách xem trước khi chưa đăng nhập.
// Khi sessionStorage.demo === '1', axios trả dữ liệu này thay vì gọi backend thật.

export const demoEmployees = [
  { employeeId: 1, firstName: 'Trần', lastName: 'Văn An', position: 'Lập trình viên Backend', department: 'Kỹ thuật', skills: 'Java, Spring Boot, PostgreSQL' },
  { employeeId: 2, firstName: 'Nguyễn', lastName: 'Thị Bình', position: 'Lập trình viên Frontend', department: 'Kỹ thuật', skills: 'React, JavaScript, Tailwind CSS' },
  { employeeId: 3, firstName: 'Lê', lastName: 'Hoàng Cường', position: 'Thiết kế UI/UX', department: 'Thiết kế', skills: 'Figma, Photoshop, CSS' },
  { employeeId: 4, firstName: 'Phạm', lastName: 'Thị Dung', position: 'Kế toán', department: 'Tài chính', skills: 'Excel, MISA, Báo cáo tài chính' },
  { employeeId: 5, firstName: 'Hoàng', lastName: 'Văn Em', position: 'Nhân viên Kinh doanh', department: 'Kinh doanh', skills: 'Đàm phán, CRM, Tiếng Anh' },
  { employeeId: 6, firstName: 'Vũ', lastName: 'Thị Hoa', position: 'Chuyên viên Nhân sự', department: 'Nhân sự', skills: 'Tuyển dụng, Đào tạo' },
]

export const demoProjects = [
  { projectId: 1, name: 'Website bán hàng', description: 'Xây dựng website thương mại điện tử', startDate: '2026-03-01', endDate: '2026-07-30', status: 'ACTIVE' },
  { projectId: 2, name: 'Ứng dụng di động', description: 'App đặt hàng cho khách hàng', startDate: '2026-04-01', endDate: '2026-09-15', status: 'IN_PROGRESS' },
  { projectId: 3, name: 'Hệ thống CRM nội bộ', description: 'Quản lý quan hệ khách hàng', startDate: '2026-02-10', endDate: '2026-05-20', status: 'ON_TRACK' },
  { projectId: 4, name: 'Chiến dịch Marketing Q2', description: 'Quảng bá dòng sản phẩm mới', startDate: '2026-04-01', endDate: '2026-06-30', status: 'COMPLETED' },
]

export const demoTasks = [
  { taskId: 1, title: 'Thiết kế cơ sở dữ liệu', description: 'Thiết kế lược đồ CSDL cho hệ thống', requiredSkills: 'PostgreSQL', dueDate: '2026-05-25', status: 'IN_PROGRESS', project: { name: 'Website bán hàng' }, assignedTo: { firstName: 'Trần', lastName: 'Văn An' } },
  { taskId: 2, title: 'Xây dựng API thanh toán', description: 'Module xử lý thanh toán đơn hàng', requiredSkills: 'Java, Spring Boot', dueDate: '2026-05-30', status: 'PENDING', project: { name: 'Website bán hàng' }, assignedTo: { firstName: 'Trần', lastName: 'Văn An' } },
  { taskId: 3, title: 'Làm giao diện trang chủ', description: 'Dựng giao diện trang chủ theo thiết kế', requiredSkills: 'React, Tailwind CSS', dueDate: '2026-05-22', status: 'IN_PROGRESS', project: { name: 'Website bán hàng' }, assignedTo: { firstName: 'Nguyễn', lastName: 'Thị Bình' } },
  { taskId: 4, title: 'Thiết kế bộ nhận diện', description: 'Logo và bộ màu thương hiệu', requiredSkills: 'Figma', dueDate: '2026-05-18', status: 'COMPLETED', project: { name: 'Chiến dịch Marketing Q2' }, assignedTo: { firstName: 'Lê', lastName: 'Hoàng Cường' } },
  { taskId: 5, title: 'Viết tài liệu hướng dẫn', description: 'Tài liệu sử dụng cho khách hàng', requiredSkills: '', dueDate: '2026-06-01', status: 'PENDING', project: { name: 'Ứng dụng di động' }, assignedTo: { firstName: 'Nguyễn', lastName: 'Thị Bình' } },
  { taskId: 6, title: 'Kiểm thử ứng dụng', description: 'Kiểm thử các luồng chính', requiredSkills: '', dueDate: '2026-06-05', status: 'PENDING', project: { name: 'Ứng dụng di động' }, assignedTo: { firstName: 'Trần', lastName: 'Văn An' } },
  { taskId: 7, title: 'Tối ưu tốc độ tải trang', description: 'Cải thiện hiệu năng frontend', requiredSkills: 'React', dueDate: '2026-05-28', status: 'IN_PROGRESS', project: { name: 'Website bán hàng' }, assignedTo: { firstName: 'Nguyễn', lastName: 'Thị Bình' } },
]

export const demoAttendance = [
  { attendanceId: 1, date: '2026-05-15', checkIn: '08:02', checkOut: '17:30', employee: { firstName: 'Trần', lastName: 'Văn An' }, checkInOffice: { name: 'Văn phòng chính' }, checkInDistanceMeters: 24, reviewStatus: 'APPROVED' },
  { attendanceId: 2, date: '2026-05-15', checkIn: '08:15', checkOut: '17:25', employee: { firstName: 'Nguyễn', lastName: 'Thị Bình' }, checkInOffice: { name: 'Văn phòng chính' }, checkInDistanceMeters: 41, reviewStatus: 'APPROVED' },
  { attendanceId: 3, date: '2026-05-15', checkIn: '09:05', checkOut: '17:40', employee: { firstName: 'Lê', lastName: 'Hoàng Cường' }, checkInOffice: { name: 'Văn phòng chính' }, checkInDistanceMeters: 320, reviewStatus: 'PENDING_REVIEW' },
  { attendanceId: 4, date: '2026-05-16', checkIn: '07:58', checkOut: '17:32', employee: { firstName: 'Trần', lastName: 'Văn An' }, checkInOffice: { name: 'Văn phòng chính' }, checkInDistanceMeters: 18, reviewStatus: 'APPROVED' },
  { attendanceId: 5, date: '2026-05-16', checkIn: '08:20', checkOut: '17:10', employee: { firstName: 'Vũ', lastName: 'Thị Hoa' }, checkInOffice: { name: 'Chi nhánh Hà Nội' }, checkInDistanceMeters: 55, reviewStatus: 'APPROVED' },
]

export const demoOffices = [
  { id: 1, name: 'Văn phòng chính', address: '123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', latitude: 10.7769, longitude: 106.7009, radiusMeters: 150, status: 'ACTIVE' },
  { id: 2, name: 'Chi nhánh Hà Nội', address: '45 Tràng Tiền, Hoàn Kiếm, Hà Nội', latitude: 21.0285, longitude: 105.8542, radiusMeters: 100, status: 'ACTIVE' },
]

export const demoUsers = [
  { userId: 1, username: 'admin', email: 'admin@example.com', role: 'ADMIN' },
  { userId: 2, username: 'quanly', email: 'manager@example.com', role: 'MANAGER' },
  { userId: 3, username: 'an.tran', email: 'an@example.com', role: 'EMPLOYEE' },
  { userId: 4, username: 'binh.nguyen', email: 'binh@example.com', role: 'EMPLOYEE' },
]

export const DEMO_LOGIN_MSG = 'Bạn cần đăng nhập để sử dụng tính năng này.'

// Trả "response" giả lập theo URL — dùng làm axios adapter khi đang ở chế độ demo.
export function demoApiResponse(config) {
  const url = config.url || ''
  const method = (config.method || 'get').toLowerCase()

  if (method !== 'get') {
    // Mọi thao tác thêm/sửa/xóa trong demo → yêu cầu đăng nhập.
    return Promise.reject({
      config,
      isDemoBlock: true,
      response: {
        status: 403,
        data: { success: false, data: null, error: DEMO_LOGIN_MSG, message: DEMO_LOGIN_MSG },
      },
    })
  }

  const wrap = (data) => Promise.resolve({
    data: { success: true, data, error: null },
    status: 200, statusText: 'OK', headers: {}, config,
  })

  if (url.includes('/employees')) return wrap(demoEmployees)
  if (url.includes('/projects')) return wrap(demoProjects)
  if (url.includes('/tasks')) return wrap(demoTasks)
  if (url.includes('/attendance')) return wrap(demoAttendance)
  if (url.includes('/office-locations')) return wrap(demoOffices)
  if (url.includes('/users')) return wrap(demoUsers)
  return wrap([])
}
