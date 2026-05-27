package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Suggestion;
import com.example.taskmanagement.repository.SuggestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;

    public SuggestionService(SuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    public List<Suggestion> getAllSuggestions() {
        return suggestionRepository.findAll();
    }

    public Suggestion getSuggestionById(Long id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found: " + id));
    }

    public Suggestion createSuggestion(Suggestion suggestion) {
        return suggestionRepository.save(suggestion);
    }

    public Suggestion submitFeedback(Long id, String feedback) {
        Suggestion suggestion = getSuggestionById(id);
        suggestion.setFeedback(feedback);
        return suggestionRepository.save(suggestion);
    }
}
