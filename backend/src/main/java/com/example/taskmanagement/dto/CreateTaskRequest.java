package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        String description,

        String requiredSkills,

        LocalDate dueDate,

        @Size(max = 50, message = "Status must not exceed 50 characters")
        String status,

        Long projectId,

        Long assignedToId
) {}
