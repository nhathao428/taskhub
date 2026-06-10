package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        String description,

        String requiredSkills,

        LocalDate dueDate,

        @Pattern(regexp = "(?i)pending|in_progress|completed",
                 message = "status must be one of: pending, in_progress, completed")
        String status,

        Long projectId,

        Long assignedToId
) {}
