package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload đặt mật khẩu mới bằng token đặt lại.
 * Quy tắc mật khẩu trùng với {@link RegisterRequestV2} — chữ + số + ký tự đặc biệt, 8..100.
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).+$",
            message = "Password must contain at least one letter, one digit, and one special character"
    )
    private String newPassword;
}
