package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.EmployeeSuggestionDTO;
import com.example.taskmanagement.dto.SuggestionRequest;
import com.example.taskmanagement.entity.Suggestion;
import com.example.taskmanagement.service.AiSuggestionService;
import com.example.taskmanagement.service.SuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;
    private final AiSuggestionService aiSuggestionService;

    public SuggestionController(SuggestionService suggestionService, AiSuggestionService aiSuggestionService) {
        this.suggestionService = suggestionService;
        this.aiSuggestionService = aiSuggestionService;
    }

    @GetMapping
    public ResponseEntity<List<Suggestion>> getAllSuggestions() {
        return ResponseEntity.ok(suggestionService.getAllSuggestions());
    }

    @PostMapping("/feedback")
    public ResponseEntity<Suggestion> submitFeedback(@RequestBody Map<String, String> body) {
        Long suggestionId = Long.parseLong(body.get("suggestion_id"));
        String feedback = body.get("feedback");
        return ResponseEntity.ok(suggestionService.submitFeedback(suggestionId, feedback));
    }

    @PostMapping("/recommend")
    public ResponseEntity<List<EmployeeSuggestionDTO>> recommendEmployees(
            @RequestBody SuggestionRequest request) {
        return ResponseEntity.ok(aiSuggestionService.recommendEmployees(request));
    }

    @GetMapping("/recommend/{taskId}")
    public ResponseEntity<List<EmployeeSuggestionDTO>> recommendForTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(aiSuggestionService.recommendEmployeesForTask(taskId));
    }
}
