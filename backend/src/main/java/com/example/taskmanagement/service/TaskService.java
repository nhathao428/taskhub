package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       EmployeeRepository employeeRepository,
                       CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.currentUserService = currentUserService;
    }

    @Cacheable("tasks")
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setStatus(request.status() != null ? request.status() : "pending");
        if (request.projectId() != null) {
            Project project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));
            task.setProject(project);
        }
        if (request.assignedToId() != null) {
            Employee employee = employeeRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.assignedToId()));
            task.setAssignedTo(employee);
        }
        return taskRepository.save(task);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public Task updateTask(Long id, UpdateTaskRequest request) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        if (request.title() != null) existing.setTitle(request.title());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.dueDate() != null) existing.setDueDate(request.dueDate());
        if (request.status() != null) existing.setStatus(request.status());
        if (request.projectId() != null) {
            Project project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));
            existing.setProject(project);
        }
        if (request.assignedToId() != null) {
            Employee employee = employeeRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.assignedToId()));
            existing.setAssignedTo(employee);
        }
        return taskRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", "id", id);
        }
        taskRepository.deleteById(id);
    }

    /** Returns all tasks assigned to the currently authenticated employee. */
    @Transactional(readOnly = true)
    public List<Task> getMyTasks(Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        return taskRepository.findByAssignedToEmployeeId(me.getEmployeeId());
    }

    /** Allows an employee to update the status of a task assigned to them. */
    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public Task updateMyTaskStatus(Long taskId, String newStatus, Authentication auth) {
        Employee me = currentUserService.getCurrentEmployee(auth);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getAssignedTo() == null
                || !task.getAssignedTo().getEmployeeId().equals(me.getEmployeeId())) {
            throw new AccessDeniedException("You can only update tasks assigned to you");
        }

        String normalized = newStatus.toLowerCase();
        task.setStatus(normalized);
        if ("completed".equals(normalized)) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        } else {
            task.setCompletedAt(null);
        }
        return taskRepository.save(task);
    }
}
