package com.example.taskmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Body cho self check-in / check-out có kèm vị trí GPS.
 * Tất cả field optional ở DTO – nếu thiếu thì backend coi như client không gửi
 * vị trí (chấp nhận, nhưng đẩy bản ghi sang PENDING_REVIEW).
 */
public record CheckInLocationRequest(
        @DecimalMin(value = "-90.0", message = "latitude phải >= -90")
        @DecimalMax(value = "90.0",  message = "latitude phải <= 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "longitude phải >= -180")
        @DecimalMax(value = "180.0",  message = "longitude phải <= 180")
        Double longitude,

        /** Client báo cáo nếu phát hiện GPS mock (mobile). */
        Boolean isMocked
) {}
