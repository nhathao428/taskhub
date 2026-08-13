package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CheckInLocationRequest;
import com.example.taskmanagement.dto.CreateAttendanceRequest;
import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.exception.BusinessException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.AttendanceRepository;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;
    private final GeofenceService geofenceService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             CurrentUserService currentUserService,
                             GeofenceService geofenceService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.currentUserService = currentUserService;
        this.geofenceService = geofenceService;
    }

    @Cacheable("attendance")
    @Transactional(readOnly = true)
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Cacheable(value = "attendance", key = "#id")
    @Transactional(readOnly = true)
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
    }

    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance logAttendance(CreateAttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(request.date());
        attendance.setCheckIn(request.checkIn());
        attendance.setCheckOut(request.checkOut());
        // Bản ghi do quản lý nhập tay coi như APPROVED.
        attendance.setReviewStatus(Attendance.ReviewStatus.APPROVED);
        return attendanceRepository.save(attendance);
    }

    @Cacheable(value = "attendance", key = "'employee-' + #employeeId")
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeEmployeeId(employeeId);
    }

    /**
     * Check-in cho 1 employee bất kỳ (manager nhập tay).
     * KHÔNG kiểm geofence – coi như đã được manager xác minh.
     */
    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(java.time.LocalDate.now());
        attendance.setCheckIn(java.time.LocalTime.now());
        attendance.setReviewStatus(Attendance.ReviewStatus.APPROVED);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkOut(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));
        attendance.setCheckOut(java.time.LocalTime.now());
        return attendanceRepository.save(attendance);
    }

    /**
     * Self check-in với GPS:
     * - Có lat/lng: tìm office gần nhất, lưu khoảng cách + office gắn được.
     *     • Nằm trong radius -> APPROVED.
     *     • Nằm ngoài radius (hoặc không có office active) -> PENDING_REVIEW.
     *     • Client báo isMocked=true -> PENDING_REVIEW (bất kể vị trí).
     * - Không có lat/lng (web không cấp quyền GPS): cho phép nhưng PENDING_REVIEW.
     */
    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkInSelf(Authentication auth, CheckInLocationRequest loc) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        LocalDate today = LocalDate.now();
        attendanceRepository
                .findFirstByEmployeeEmployeeIdAndDateAndCheckOutIsNullOrderByCheckInDesc(me.getEmployeeId(), today)
                .ifPresent(a -> {
                    throw new BusinessException("Already checked in today and not yet checked out");
                });

        Attendance a = new Attendance();
        a.setEmployee(me);
        a.setDate(LocalDate.now());
        a.setCheckIn(java.time.LocalTime.now());
        applyLocation(a, loc);
        return attendanceRepository.save(a);
    }

    /** Self check-out: closes today's open check-in for the authenticated employee. */
    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkOutSelf(Authentication auth, CheckInLocationRequest loc) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        LocalDate today = LocalDate.now();
        Attendance open = attendanceRepository
                .findFirstByEmployeeEmployeeIdAndDateAndCheckOutIsNullOrderByCheckInDesc(me.getEmployeeId(), today)
                .orElseThrow(() -> new BusinessException("No open check-in for today"));
        open.setCheckOut(java.time.LocalTime.now());

        // Xử lý vị trí check-out (không thay đổi reviewStatus đã có lúc check-in).
        // KHÔNG lưu toạ độ GPS thô vào DB nữa (audit bảo mật 8/2026) — chỉ dùng lat/lng
        // trong bộ nhớ lúc xử lý request này, không set lên entity. Xem applyLocation()
        // để biết lý do đầy đủ.
        if (loc != null) {
            if (Boolean.TRUE.equals(loc.isMocked())) {
                open.setIsMocked(true);
                open.setReviewStatus(Attendance.ReviewStatus.PENDING_REVIEW);
            }
        }
        return attendanceRepository.save(open);
    }

    /**
     * Gán thông tin vị trí + reviewStatus cho bản ghi check-in mới.
     *
     * QUAN TRỌNG (audit bảo mật 8/2026): KHÔNG còn lưu toạ độ GPS thô (lat/lng) vào DB.
     * Vẫn nhận GPS từ client và vẫn dùng để so khớp bán kính văn phòng như trước (không đổi
     * logic chống gian lận) — chỉ khác là sau khi tính ra kết quả (APPROVED/PENDING_REVIEW +
     * khoảng cách mét), toạ độ chính xác KHÔNG được ghi lên entity nữa, chỉ tồn tại tạm trong
     * bộ nhớ lúc xử lý request này. Lý do: lịch sử vị trí chi tiết, giữ vĩnh viễn, Manager nào
     * cũng xem được toàn bộ — là dữ liệu nhạy cảm nhất trong hệ thống nếu DB bị rò rỉ, trong
     * khi nghiệp vụ chỉ thực sự cần biết "trong hay ngoài bán kính", không cần toạ độ chính xác.
     * Cột check_in_lat/check_in_lng/check_out_lat/check_out_lng vẫn giữ trong schema (không
     * migration xoá cột — tránh rủi ro không cần thiết) nhưng từ nay luôn là null.
     */
    private void applyLocation(Attendance a, CheckInLocationRequest loc) {
        if (loc == null || loc.latitude() == null || loc.longitude() == null) {
            // Không có GPS -> đẩy lên review.
            a.setReviewStatus(Attendance.ReviewStatus.PENDING_REVIEW);
            return;
        }
        a.setIsMocked(Boolean.TRUE.equals(loc.isMocked()));

        // Mock location => bắt buộc review.
        if (Boolean.TRUE.equals(loc.isMocked())) {
            a.setReviewStatus(Attendance.ReviewStatus.PENDING_REVIEW);
            return;
        }

        Optional<GeofenceService.Match> match = geofenceService
                .findNearestActive(loc.latitude(), loc.longitude());
        if (match.isEmpty()) {
            // Chưa có office nào active -> review.
            a.setReviewStatus(Attendance.ReviewStatus.PENDING_REVIEW);
            return;
        }
        GeofenceService.Match m = match.get();
        a.setCheckInOffice(m.office());
        a.setCheckInDistanceMeters((int) Math.round(m.distanceMeters()));
        a.setReviewStatus(m.withinRadius()
                ? Attendance.ReviewStatus.APPROVED
                : Attendance.ReviewStatus.PENDING_REVIEW);
    }

    /** Manager duyệt hoặc từ chối 1 bản ghi đang PENDING_REVIEW. */
    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance review(Long attendanceId, Attendance.ReviewStatus newStatus) {
        Attendance a = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));
        if (a.getReviewStatus() != Attendance.ReviewStatus.PENDING_REVIEW) {
            throw new BusinessException("Bản ghi không ở trạng thái PENDING_REVIEW");
        }
        a.setReviewStatus(newStatus);
        return attendanceRepository.save(a);
    }

    /** Returns attendance history for the currently authenticated employee. */
    @Transactional(readOnly = true)
    public List<Attendance> getMyAttendance(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        return attendanceRepository.findByEmployeeEmployeeIdOrderByDateDescCheckInDesc(me.getEmployeeId());
    }
}
