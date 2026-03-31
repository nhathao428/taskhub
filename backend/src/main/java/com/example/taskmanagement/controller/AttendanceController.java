package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.CheckInRequest;
import com.example.taskmanagement.dto.CheckOutRequest;
import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<Attendance>> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(attendanceService.checkIn(request.employeeId())));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Attendance>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.checkOut(request.attendanceId())));
    }
}
