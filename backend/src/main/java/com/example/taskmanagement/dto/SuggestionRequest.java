package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuggestionRequest {
    private Long taskId;
    @NotBlank(message = "Task title is required")
    private String taskTitle;
    private String taskDescription;
    private Long projectId;

    public String getCacheKey() {
        return taskId != null ? "id-" + taskId : "title-" + taskTitle;
    }
}
