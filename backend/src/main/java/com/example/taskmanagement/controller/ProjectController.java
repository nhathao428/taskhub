package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.CreateProjectRequest;
import com.example.taskmanagement.dto.UpdateProjectRequest;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllProjects(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProject(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectById(id, authentication)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Project>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(projectService.createProject(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.updateProject(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
