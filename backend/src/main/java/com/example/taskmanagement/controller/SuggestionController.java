package com.example.taskmanagement.controller;

import com.example.taskmanagement.entity.Suggestion;
import com.example.taskmanagement.service.SuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
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
}
