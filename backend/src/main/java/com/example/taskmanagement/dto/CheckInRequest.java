package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull(message = "employeeId is required")
        Long employeeId
) {}
