package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        String description,

        // DB có ràng buộc start_date NOT NULL — trước đây field này optional ở DTO,
        // khiến request thiếu startDate lọt qua validation rồi văng lỗi SQL ở tầng DB,
        // bị GlobalExceptionHandler dịch thành 409 "không thể xóa..." (message dành cho
        // DataIntegrityViolationException nói chung, gây hiểu lầm cho lỗi tạo mới). Thêm
        // @NotNull để chặn sớm ở tầng validate, trả 400 rõ ràng thay vì 409 khó hiểu.
        @NotNull(message = "startDate is required")
        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 50, message = "Status must not exceed 50 characters")
        String status,

        @Size(max = 100, message = "Group must not exceed 100 characters")
        String group
) {}
