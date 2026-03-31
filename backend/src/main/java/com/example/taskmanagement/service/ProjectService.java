package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateProjectRequest;
import com.example.taskmanagement.dto.UpdateProjectRequest;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.ProjectRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Cacheable("projects")
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Cacheable(value = "projects", key = "#id")
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    @CacheEvict(value = "projects", allEntries = true)
    public Project createProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setStatus(request.status() != null ? request.status() : "ongoing");
        return projectRepository.save(project);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public Project updateProject(Long id, UpdateProjectRequest request) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        if (request.name() != null) existing.setName(request.name());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.startDate() != null) existing.setStartDate(request.startDate());
        if (request.endDate() != null) existing.setEndDate(request.endDate());
        if (request.status() != null) existing.setStatus(request.status());
        return projectRepository.save(existing);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
