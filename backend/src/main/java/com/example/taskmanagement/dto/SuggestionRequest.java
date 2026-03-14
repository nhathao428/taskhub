package com.example.taskmanagement.dto;

import lombok.Data;

import java.util.List;

@Data
public class SuggestionRequest {
    private Long taskId;
    private String taskTitle;
    private String taskDescription;
    private List<String> requiredSkills;
    private Long projectId;
}
