package com.example.taskmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Body cho self check-in / check-out có kèm vị trí GPS và (tuỳ chọn) nhận diện khuôn mặt.
 *
 * Tất cả field đều optional – nếu thiếu vị trí thì backend coi như client không gửi GPS
 * (chấp nhận, nhưng đẩy bản ghi sang PENDING_REVIEW).
 *
 * Hai field khuôn mặt được thêm sau (8/2026) và cũng optional, nên client cũ không gửi
 * vẫn chạy y như trước — không phá vỡ app đang dùng.
 */
public record CheckInLocationRequest(
        @DecimalMin(value = "-90.0", message = "latitude phải >= -90")
        @DecimalMax(value = "90.0",  message = "latitude phải <= 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "longitude phải >= -180")
        @DecimalMax(value = "180.0",  message = "longitude phải <= 180")
        Double longitude,

        /** Client báo cáo nếu phát hiện GPS mock (mobile). */
        Boolean isMocked,

        /**
         * Ảnh khuôn mặt chụp lúc check-in, encode base64 (chấp nhận cả tiền tố data URI).
         * Bỏ trống = check-in chỉ bằng GPS như trước.
         * Ảnh KHÔNG được lưu lại — huỷ ngay sau khi so khớp xong.
         */
        String faceImageBase64,

        /**
         * Nhiều khung hình liên tiếp để kiểm tra chống giả mạo (phát hiện chớp mắt).
         * Cần ít nhất 3 khung. Bỏ trống = bỏ qua bước liveness (xem app.face.require-liveness).
         */
        @Size(max = 30, message = "Tối đa 30 khung hình cho kiểm tra chống giả mạo")
        List<String> livenessFramesBase64
) {}
