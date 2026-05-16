package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Size(max = 50, message = "Position must not exceed 50 characters")
        String position,

        @Size(max = 50, message = "Department must not exceed 50 characters")
        String department,

        @Size(max = 100, message = "Group must not exceed 100 characters")
        String group,

        String skills,

        /** Email tài khoản user để liên kết hồ sơ nhân viên (tùy chọn). */
        String userEmail
) {}
