package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.dto.UpdateTaskRequest;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getAllTasks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTaskById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(taskService.createTask(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.updateTask(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
