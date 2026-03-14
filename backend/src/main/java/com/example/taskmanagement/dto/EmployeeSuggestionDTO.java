package com.example.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSuggestionDTO {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String department;
    private double skillMatchScore;
    private double workloadScore;
    private double performanceScore;
    private double attendanceScore;
    private double overallScore;
    private String reasoning;
}
