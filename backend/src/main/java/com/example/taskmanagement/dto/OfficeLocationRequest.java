package com.example.taskmanagement.dto;

import jakarta.validation.constraints.*;

public record OfficeLocationRequest(
        @NotBlank(message = "name là bắt buộc")
        @Size(max = 200)
        String name,

        @Size(max = 500)
        String address,

        @NotNull(message = "latitude là bắt buộc")
        @DecimalMin(value = "-90.0",  message = "latitude phải >= -90")
        @DecimalMax(value = "90.0",   message = "latitude phải <= 90")
        Double latitude,

        @NotNull(message = "longitude là bắt buộc")
        @DecimalMin(value = "-180.0", message = "longitude phải >= -180")
        @DecimalMax(value = "180.0",  message = "longitude phải <= 180")
        Double longitude,

        @Min(value = 10,   message = "radius tối thiểu 10m")
        @Max(value = 5000, message = "radius tối đa 5000m")
        Integer radiusMeters,

        /** ACTIVE / INACTIVE (mặc định ACTIVE khi tạo mới). */
        String status
) {}
