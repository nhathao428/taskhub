package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Đăng ký khuôn mặt: gửi lên vài ảnh (base64) của cùng một người.
 * Backend trích embedding từng ảnh rồi lấy trung bình — nhiều ảnh cho kết quả ổn định hơn.
 * Ảnh KHÔNG được lưu lại, huỷ ngay sau khi trích xuất xong.
 */
public record FaceEnrollRequest(
        @NotEmpty(message = "Cần ít nhất 1 ảnh")
        @Size(max = 10, message = "Tối đa 10 ảnh mỗi lần đăng ký")
        List<String> imagesBase64
) {}
