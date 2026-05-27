package com.example.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * v2 register payload — strict validation:
 *   - 8..100 ký tự
 *   - phải có chữ cái, số, và ký tự đặc biệt (!@#$%^&*()_+-=[]{};':\"\\|,.<>/?`~)
 *
 * Endpoint: POST /api/v2/auth/register
 * App cũ trên thiết bị vẫn dùng {@link RegisterRequest} qua /api/auth/register.
 */
@Data
public class RegisterRequestV2 {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).+$",
            message = "Password must contain at least one letter, one digit, and one special character"
    )
    private String password;

    /** Convert v2 payload sang v1 DTO để tái sử dụng UserService.register(). */
    public RegisterRequest toV1() {
        RegisterRequest v1 = new RegisterRequest();
        v1.setUsername(username);
        v1.setEmail(email);
        v1.setPassword(password);
        return v1;
    }
}
