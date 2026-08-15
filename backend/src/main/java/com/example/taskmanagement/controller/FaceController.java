package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.FaceEnrollRequest;
import com.example.taskmanagement.dto.FaceEnrollResponse;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.EmployeeFace;
import com.example.taskmanagement.service.CurrentUserService;
import com.example.taskmanagement.service.FaceRecognitionService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Đăng ký / quản lý khuôn mặt dùng cho check-in.
 *
 * Nhân viên tự đăng ký khuôn mặt của CHÍNH MÌNH (/me) — không ai đăng ký hộ người khác
 * được, tránh việc dựng sẵn khuôn mặt giả cho tài khoản người khác.
 * Quản lý chỉ có quyền XOÁ đăng ký (khi nhân viên nghỉ việc hoặc cần đăng ký lại).
 */
@RestController
@RequestMapping("/api/face")
public class FaceController {

    private final FaceRecognitionService faceService;
    private final CurrentUserService currentUserService;

    public FaceController(FaceRecognitionService faceService, CurrentUserService currentUserService) {
        this.faceService = faceService;
        this.currentUserService = currentUserService;
    }

    /** Trạng thái tính năng + đã đăng ký khuôn mặt hay chưa. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> myFaceStatus(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "featureEnabled", faceService.isEnabled(),
                "enrolled", faceService.isEnrolled(me.getEmployeeId()),
                "threshold", faceService.getThreshold(),
                "livenessRequired", faceService.isLivenessRequired()
        )));
    }

    /** Đăng ký (hoặc đăng ký lại, ghi đè) khuôn mặt của chính mình. */
    @PostMapping("/me/enroll")
    public ResponseEntity<ApiResponse<FaceEnrollResponse>> enrollMyFace(
            Authentication auth,
            @Valid @RequestBody FaceEnrollRequest request) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        EmployeeFace face = faceService.enroll(me, request.imagesBase64());
        FaceEnrollResponse body = new FaceEnrollResponse(
                me.getEmployeeId(),
                face.getSampleCount(),
                face.getEnrolledAt(),
                "Đăng ký khuôn mặt thành công từ " + face.getSampleCount() + " ảnh hợp lệ");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body));
    }

    /** Nhân viên tự xoá dữ liệu khuôn mặt của mình (quyền với dữ liệu sinh trắc học cá nhân). */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyFace(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        faceService.deleteEnrollment(me.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.ok("Đã xoá dữ liệu khuôn mặt"));
    }

    /** Quản lý xoá đăng ký của một nhân viên (nghỉ việc, hoặc cần đăng ký lại). */
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteEmployeeFace(@PathVariable Long employeeId) {
        faceService.deleteEnrollment(employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xoá dữ liệu khuôn mặt của nhân viên " + employeeId));
    }

    /**
     * Ảnh của lần check-in BỊ NGHI VẤN, để quản lý nhìn và tự phán đoán đúng/sai người.
     *
     * Chỉ ADMIN/MANAGER xem được. Chỉ tồn tại với lần check-in không khớp mặt hoặc trượt
     * liveness — lần hợp lệ không lưu ảnh. Ảnh tự xoá sau app.face.capture-retention-days.
     *
     * Trả về ảnh nhị phân trực tiếp (không phải JSON) để frontend gắn thẳng vào thẻ img.
     */
    @GetMapping("/capture/{attendanceId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> getCapture(@PathVariable Long attendanceId) {
        byte[] image = faceService.getCaptureImage(attendanceId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                // Ảnh sinh trắc học: cấm cache ở trình duyệt/proxy trung gian.
                .cacheControl(CacheControl.noStore())
                .body(image);
    }
}
