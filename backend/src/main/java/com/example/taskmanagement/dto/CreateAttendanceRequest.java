package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAttendanceRequest(
    @NotNull(message = "Employee ID is required") Long employeeId,
    @NotNull(message = "Date is required") LocalDate date,
    LocalTime checkIn,
    LocalTime checkOut
) {}
