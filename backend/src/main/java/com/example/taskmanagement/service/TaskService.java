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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Transactional(readOnly = true)
    public List<Task> getAllTasks(Authentication auth) {
        if (isManagerOrAdmin(auth)) {
            return taskRepository.findAll();
        }
        // EMPLOYEE: pushdown filter về DB (xem TaskRepository.findOwnedOrSameGroup).
        Employee me = currentUserService.getCurrentEmployee(auth);
        String group = (me.getGroup() != null && !me.getGroup().isBlank()) ? me.getGroup() : null;
        return taskRepository.findOwnedOrSameGroup(me.getEmployeeId(), group);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id, Authentication auth) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        if (isManagerOrAdmin(auth)) {
            return task;
        }
        Employee me = currentUserService.getCurrentEmployee(auth);
        if (!isOwnedOrSameGroup(task, me)) {
            throw new AccessDeniedException("You can only view tasks assigned to you or in your group");
        }
        return task;
    }

    private boolean isManagerOrAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))
                || auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private boolean isOwnedOrSameGroup(Task task, Employee me) {
        boolean assignedToMe = task.getAssignedTo() != null
                && me.getEmployeeId().equals(task.getAssignedTo().getEmployeeId());
        boolean sameGroup = task.getProject() != null
                && me.getGroup() != null
                && !me.getGroup().isBlank()
                && me.getGroup().equals(task.getProject().getGroup());
        return assignedToMe || sameGroup;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setRequiredSkills(request.requiredSkills());
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
        if (request.requiredSkills() != null) existing.setRequiredSkills(request.requiredSkills());
        if (request.dueDate() != null) existing.setDueDate(request.dueDate());
        if (request.status() != null) {
            String normalized = request.status().toLowerCase();
            existing.setStatus(normalized);
            // Đồng bộ completedAt như updateMyTaskStatus để Manager close task qua PUT
            // cũng tạo dấu thời gian hoàn thành — tránh báo cáo "đã hoàn thành nhưng
            // không có completedAt".
            if ("completed".equals(normalized)) {
                if (existing.getCompletedAt() == null) {
                    existing.setCompletedAt(LocalDateTime.now());
                }
            } else {
                existing.setCompletedAt(null);
            }
        }
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
