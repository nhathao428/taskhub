package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.repository.AttendanceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Cacheable("attendance")
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Cacheable(value = "attendance", key = "#id")
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + id));
    }

    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance logAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Cacheable(value = "attendance", key = "'employee-' + #employeeId")
    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeEmployeeId(employeeId);
    }

    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkIn(Long employeeId) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(java.time.LocalDate.now());
        attendance.setCheckIn(java.time.LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    @CacheEvict(value = "attendance", allEntries = true)
    public Attendance checkOut(Long attendanceId) {
        Attendance attendance = getAttendanceById(attendanceId);
        attendance.setCheckOut(java.time.LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }
}
