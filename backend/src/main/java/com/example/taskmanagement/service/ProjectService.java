package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Project;
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
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
    }

    @CacheEvict(value = "projects", allEntries = true)
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public Project updateProject(Long id, Project updated) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setStatus(updated.getStatus());
        return projectRepository.save(existing);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
