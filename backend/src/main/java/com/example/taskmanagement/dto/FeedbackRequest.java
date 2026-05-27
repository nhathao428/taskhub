package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body cho POST /api/suggestions/feedback.
 * Có @Size để tránh client gửi feedback 10MB → spam DB cột TEXT (Security L4).
 */
public record FeedbackRequest(
        @NotNull(message = "suggestion_id là bắt buộc")
        @Positive(message = "suggestion_id phải là số dương")
        Long suggestionId,

        @NotBlank(message = "feedback là bắt buộc")
        @Size(max = 2000, message = "feedback tối đa 2000 ký tự")
        String feedback
) {}
