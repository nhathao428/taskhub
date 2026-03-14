package com.example.taskmanagement.controller;

import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @PostMapping
    public ResponseEntity<Attendance> logAttendance(@RequestBody Attendance attendance) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.logAttendance(attendance));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId));
    }
}
