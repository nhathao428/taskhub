package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateProjectRequest;
import com.example.taskmanagement.dto.UpdateProjectRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          EmployeeRepository employeeRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    /** Returns true when the caller holds MANAGER or ADMIN role. */
    private boolean isManagerOrAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))
                || auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /** Resolves the group of the currently authenticated EMPLOYEE. */
    private Optional<String> resolveEmployeeGroup(Authentication auth) {
        if (auth == null) return Optional.empty();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .flatMap(employeeRepository::findByUser)
                .map(Employee::getGroup);
    }

    public List<Project> getAllProjects(Authentication auth) {
        if (isManagerOrAdmin(auth)) {
            return projectRepository.findAll();
        }
        // EMPLOYEE: return only projects belonging to their group
        Optional<String> group = resolveEmployeeGroup(auth);
        if (group.isEmpty() || group.get() == null || group.get().isBlank()) {
            return List.of();
        }
        return projectRepository.findByGroup(group.get());
    }

    public Project getProjectById(Long id, Authentication auth) {
        if (isManagerOrAdmin(auth)) {
            return projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        }
        // EMPLOYEE: ensure project belongs to their group
        Optional<String> group = resolveEmployeeGroup(auth);
        if (group.isEmpty() || group.get() == null || group.get().isBlank()) {
            throw new AccessDeniedException("No group assigned to employee");
        }
        return projectRepository.findByProjectIdAndGroup(id, group.get())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public Project createProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setStatus(request.status() != null ? request.status() : "ongoing");
        project.setGroup(request.group());
        return projectRepository.save(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public Project updateProject(Long id, UpdateProjectRequest request) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        if (request.name() != null) existing.setName(request.name());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.startDate() != null) existing.setStartDate(request.startDate());
        if (request.endDate() != null) existing.setEndDate(request.endDate());
        if (request.status() != null) existing.setStatus(request.status());
        if (request.group() != null) existing.setGroup(request.group());
        return projectRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }
        projectRepository.deleteById(id);
    }
}
