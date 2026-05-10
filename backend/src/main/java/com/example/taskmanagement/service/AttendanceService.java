package com.example.taskmanagement.service;

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

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             CurrentUserService currentUserService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.currentUserService = currentUserService;
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
        return attendanceRepository.save(attendance);
    }

    @Cacheable(value = "attendance", key = "'employee-' + #employeeId")
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeEmployeeId(employeeId);
    }

    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(java.time.LocalDate.now());
        attendance.setCheckIn(java.time.LocalTime.now());
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

    /** Self check-in for the currently authenticated employee. */
    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkInSelf(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        LocalDate today = LocalDate.now();
        attendanceRepository
                .findFirstByEmployeeEmployeeIdAndDateAndCheckOutIsNullOrderByCheckInDesc(me.getEmployeeId(), today)
                .ifPresent(a -> {
                    throw new BusinessException("Already checked in today and not yet checked out");
                });
        return checkIn(me.getEmployeeId());
    }

    /** Self check-out: closes today's open check-in for the authenticated employee. */
    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkOutSelf(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        LocalDate today = LocalDate.now();
        Attendance open = attendanceRepository
                .findFirstByEmployeeEmployeeIdAndDateAndCheckOutIsNullOrderByCheckInDesc(me.getEmployeeId(), today)
                .orElseThrow(() -> new BusinessException("No open check-in for today"));
        open.setCheckOut(java.time.LocalTime.now());
        return attendanceRepository.save(open);
    }

    /** Returns attendance history for the currently authenticated employee. */
    @Transactional(readOnly = true)
    public List<Attendance> getMyAttendance(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        return attendanceRepository.findByEmployeeEmployeeIdOrderByDateDescCheckInDesc(me.getEmployeeId());
    }
}
