package com.example.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phản hồi cho /forgot-password.
 *
 * {@code message} luôn generic (anti-enumeration): không tiết lộ email có tồn tại hay không.
 * {@code resetToken}/{@code resetLink} CHỈ có giá trị ở môi trường dev (khi
 * {@code app.password-reset.expose-token=true}) vì hệ thống chưa gửi email thật.
 * Ở production phải tắt cờ này — token đi qua email, không bao giờ lọt vào response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordResponse {
    private String message;
    private String resetToken;
    private String resetLink;
}
