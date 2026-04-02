package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateAttendanceRequest(
    @NotNull(message = "Employee ID is required") Long employeeId,
    @NotNull(message = "Date is required") LocalDate date,
    LocalDateTime checkIn,
    LocalDateTime checkOut
) {}
