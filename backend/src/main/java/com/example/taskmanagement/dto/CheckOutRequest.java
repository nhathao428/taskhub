package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotNull;

public record CheckOutRequest(
        @NotNull(message = "attendanceId is required")
        Long attendanceId
) {}
