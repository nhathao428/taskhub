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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    @Cacheable("tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Cacheable(value = "tasks", key = "#id")
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
}
