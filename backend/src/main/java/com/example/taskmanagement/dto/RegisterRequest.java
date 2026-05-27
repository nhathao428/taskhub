package com.example.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * v1 register payload — KHÔNG đổi validate, app cũ trên thiết bị (Mobile/Frontend
 * phiên bản trước) đang dựa vào quy tắc 6+ ký tự. Đổi sẽ làm app cũ bị 400.
 * Validate mạnh hơn nằm ở {@link RegisterRequestV2} cho /api/v2/auth/register.
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
