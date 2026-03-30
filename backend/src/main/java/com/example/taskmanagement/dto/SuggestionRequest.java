package com.example.taskmanagement.dto;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class SuggestionRequest {
    private Long taskId;
    private String taskTitle;
    private String taskDescription;
    private List<String> requiredSkills;
    private Long projectId;

    public String getCacheKey() {
        String sortedSkills = requiredSkills == null ? "" :
                requiredSkills.stream().sorted().collect(Collectors.joining(","));
        return taskTitle + "-" + sortedSkills;
    }
}
