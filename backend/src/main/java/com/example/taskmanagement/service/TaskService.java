package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Cacheable("tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Cacheable(value = "tasks", key = "#id")
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public Task updateTask(Long id, Task updated) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setProject(updated.getProject());
        existing.setAssignedTo(updated.getAssignedTo());
        return taskRepository.save(existing);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
