package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.EmployeeSuggestionDTO;
import com.example.taskmanagement.dto.SuggestionRequest;
import com.example.taskmanagement.entity.Suggestion;
import com.example.taskmanagement.service.AiSuggestionService;
import com.example.taskmanagement.service.SuggestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private static final Logger log = LoggerFactory.getLogger(SuggestionController.class);

    private final SuggestionService suggestionService;
    private final AiSuggestionService aiSuggestionService;

    public SuggestionController(SuggestionService suggestionService, AiSuggestionService aiSuggestionService) {
        this.suggestionService = suggestionService;
        this.aiSuggestionService = aiSuggestionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Suggestion>>> getAllSuggestions() {
        return ResponseEntity.ok(ApiResponse.ok(suggestionService.getAllSuggestions()));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<Suggestion>> submitFeedback(@RequestBody Map<String, String> body) {
        String suggestionIdStr = body.get("suggestion_id");
        if (suggestionIdStr == null || suggestionIdStr.isBlank()) {
            throw new IllegalArgumentException("suggestion_id is required");
        }
        String feedback = body.get("feedback");
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("feedback is required");
        }
        Long suggestionId = Long.parseLong(suggestionIdStr);
        return ResponseEntity.ok(ApiResponse.ok(suggestionService.submitFeedback(suggestionId, feedback)));
    }

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<List<EmployeeSuggestionDTO>>> recommendEmployees(
            @Valid @RequestBody SuggestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiSuggestionService.recommendEmployees(request)));
    }

    @GetMapping("/recommend/{taskId}")
    public ResponseEntity<ApiResponse<List<EmployeeSuggestionDTO>>> recommendForTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.ok(aiSuggestionService.recommendEmployeesForTask(taskId)));
    }
}
