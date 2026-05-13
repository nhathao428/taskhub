package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.CheckInLocationRequest;
import com.example.taskmanagement.dto.CheckInRequest;
import com.example.taskmanagement.dto.CheckOutRequest;
import com.example.taskmanagement.dto.CreateAttendanceRequest;
import com.example.taskmanagement.dto.ReviewAttendanceRequest;
import com.example.taskmanagement.entity.Attendance;
import com.example.taskmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ApiResponse<Attendance>> logAttendance(@Valid @RequestBody CreateAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(attendanceService.logAttendance(request)));
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Attendance>>> getMyAttendance(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getMyAttendance(auth)));
    }

    /**
     * Self check-in: body chứa lat/lng + isMocked tuỳ chọn.
     * Backward-compatible – body có thể null/empty.
     */
    @PostMapping("/me/checkin")
    public ResponseEntity<ApiResponse<Attendance>> checkInSelf(
            Authentication auth,
            @RequestBody(required = false) @Valid CheckInLocationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(attendanceService.checkInSelf(auth, req)));
    }

    @PostMapping("/me/checkout")
    public ResponseEntity<ApiResponse<Attendance>> checkOutSelf(
            Authentication auth,
            @RequestBody(required = false) @Valid CheckInLocationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.checkOutSelf(auth, req)));
    }

    /** Manager duyệt / từ chối bản ghi PENDING_REVIEW. */
    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<Attendance>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAttendanceRequest req) {
        Attendance.ReviewStatus status = Attendance.ReviewStatus.valueOf(req.status());
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.review(id, status)));
    }
}
