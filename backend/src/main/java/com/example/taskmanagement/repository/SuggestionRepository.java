package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Suggestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Suggestion> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Suggestion> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Suggestion> findByUserUserId(Long userId);
}
