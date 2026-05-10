package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateTaskStatusRequest(
        @NotBlank(message = "status is required")
        @Pattern(regexp = "(?i)pending|in_progress|completed", message = "status must be one of: pending, in_progress, completed")
        String status
) {}
