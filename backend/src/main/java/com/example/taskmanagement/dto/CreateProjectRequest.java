package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        String description,

        LocalDateTime startDate,

        LocalDateTime endDate,

        @Size(max = 50, message = "Status must not exceed 50 characters")
        String status
) {}
