package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateAttendanceRequest;
import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.AttendanceRepository;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Cacheable("attendance")
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Cacheable(value = "attendance", key = "#id")
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
        attendance.setCheckIn(java.time.LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    @Transactional
    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkOut(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));
        attendance.setCheckOut(java.time.LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }
}
