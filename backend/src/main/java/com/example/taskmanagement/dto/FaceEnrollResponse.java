package com.example.taskmanagement.dto;

import java.time.LocalDateTime;

/**
 * Kết quả đăng ký khuôn mặt. KHÔNG chứa embedding hay ảnh — chỉ thông tin trạng thái.
 */
public record FaceEnrollResponse(
        Long employeeId,
        int sampleCount,
        LocalDateTime enrolledAt,
        String message
) {}
