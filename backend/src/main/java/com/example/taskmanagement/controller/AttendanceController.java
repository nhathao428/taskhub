package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.exception.BusinessException;
import com.example.taskmanagement.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Attendance>>> getAllAttendance() {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAllAttendance()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Attendance>> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAttendanceById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Attendance>> logAttendance(@RequestBody Attendance attendance) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(attendanceService.logAttendance(attendance)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAttendanceByEmployee(employeeId)));
    }

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<Attendance>> checkIn(@RequestBody Map<String, Long> body) {
        Long employeeId = body.get("employeeId");
        if (employeeId == null) {
            throw new BusinessException("employeeId is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(attendanceService.checkIn(employeeId)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Attendance>> checkOut(@RequestBody Map<String, Long> body) {
        Long attendanceId = body.get("attendanceId");
        if (attendanceId == null) {
            throw new BusinessException("attendanceId is required");
        }
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.checkOut(attendanceId)));
    }
}
