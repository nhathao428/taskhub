package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TaskService taskService;

    /** getAllTasks → trả về danh sách đúng size */
    @Test
    void testGetAllTasks() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        Task task2 = new Task();
        task2.setTitle("Task 2");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepository).findAll();
    }

    /** getTaskById với id tồn tại → trả về Task đúng */
    @Test
    void testGetTaskById_Found() {
        Task task = new Task();
        task.setTitle("My Task");
        task.setStatus("pending");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals("My Task", result.getTitle());
    }

    /** getTaskById với id không tồn tại → ném ResourceNotFoundException */
    @Test
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L));
    }

    /** createTask không có project/assignee → save() được gọi */
    @Test
    void testCreateTask_Simple() {
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task", "Description", LocalDateTime.now(), "pending", null, null);

        Task saved = new Task();
        saved.setTitle("New Task");
        saved.setStatus("pending");

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    /** deleteTask → deleteById được gọi với đúng id */
    @Test
    void testDeleteTask() {
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }
}
