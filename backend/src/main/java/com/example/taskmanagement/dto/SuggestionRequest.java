package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuggestionRequest {
    private Long taskId;
    @NotBlank(message = "Task title is required")
    private String taskTitle;
    private String taskDescription;
    private String requiredSkills;
    private Long projectId;

    /**
     * Cache key normalize: lowercase + collapse whitespace + truncate.
     * Cùng prompt với khoảng trắng/case khác nhau → cùng cache entry, tránh cache spam (Security M5).
     */
    public String getCacheKey() {
        if (taskId != null) return "id-" + taskId;
        String title = taskTitle == null ? "" : taskTitle;
        String norm = title.toLowerCase().replaceAll("\\s+", " ").trim();
        if (norm.length() > 200) norm = norm.substring(0, 200);
        return "title-" + Integer.toHexString(norm.hashCode());
    }
}
