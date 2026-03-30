package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.EmployeeSuggestionDTO;
import com.example.taskmanagement.dto.ErrorResponse;
import com.example.taskmanagement.dto.SuggestionRequest;
import com.example.taskmanagement.entity.Suggestion;
import com.example.taskmanagement.service.AiSuggestionService;
import com.example.taskmanagement.service.SuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error in suggestions endpoint", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred. Please try again later.", LocalDateTime.now()));
    }
}
