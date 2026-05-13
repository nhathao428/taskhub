package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReviewAttendanceRequest(
        @NotBlank(message = "status là bắt buộc")
        @Pattern(regexp = "APPROVED|REJECTED", message = "status phải là APPROVED hoặc REJECTED")
        String status
) {}
