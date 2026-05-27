package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Project;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
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

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TaskService taskService;

    private Authentication managerAuth() {
        return new UsernamePasswordAuthenticationToken(
                "manager", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    private Authentication employeeAuth() {
        return new UsernamePasswordAuthenticationToken(
                "employee", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    private Employee employee(Long id, String group) {
        Employee e = new Employee();
        e.setEmployeeId(id);
        e.setGroup(group);
        return e;
    }

    private Task taskWithProjectGroup(Long id, String projectGroup) {
        Task t = new Task();
        t.setTaskId(id);
        Project p = new Project();
        p.setGroup(projectGroup);
        t.setProject(p);
        return t;
    }

    private Task taskAssignedTo(Long id, Employee assignee) {
        Task t = new Task();
        t.setTaskId(id);
        t.setAssignedTo(assignee);
        return t;
    }

    /** getAllTasks (MANAGER) → trả về danh sách đúng size */
    @Test
    void testGetAllTasks() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        Task task2 = new Task();
        task2.setTitle("Task 2");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.getAllTasks(managerAuth());

        assertEquals(2, result.size());
        verify(taskRepository).findAll();
    }

    /** getTaskById với id tồn tại (MANAGER) → trả về Task đúng */
    @Test
    void testGetTaskById_Found() {
        Task task = new Task();
        task.setTitle("My Task");
        task.setStatus("pending");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L, managerAuth());

        assertNotNull(result);
        assertEquals("My Task", result.getTitle());
    }

    /** getTaskById với id không tồn tại → ném ResourceNotFoundException */
    @Test
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L, managerAuth()));
    }

    /** createTask không có project/assignee → save() được gọi */
    @Test
    void testCreateTask_Simple() {
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task", "Description", null,
                LocalDate.now(), "pending", null, null);

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
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    /** EMPLOYEE: getAllTasks chỉ trả về task assignedTo mình HOẶC cùng project group */
    @Test
    void testGetAllTasks_EmployeeFiltersOutOtherGroups() {
        Employee me = employee(10L, "alpha");

        Task assignedToMe = taskAssignedTo(1L, me);
        Task sameGroup = taskWithProjectGroup(2L, "alpha");
        Task otherGroup = taskWithProjectGroup(3L, "beta");
        Task noProjectNoAssignee = new Task();
        noProjectNoAssignee.setTaskId(4L);

        when(currentUserService.getCurrentEmployee(any(Authentication.class))).thenReturn(me);
        when(taskRepository.findAll())
                .thenReturn(Arrays.asList(assignedToMe, sameGroup, otherGroup, noProjectNoAssignee));

        List<Task> result = taskService.getAllTasks(employeeAuth());

        assertEquals(2, result.size());
        assertTrue(result.contains(assignedToMe));
        assertTrue(result.contains(sameGroup));
        assertFalse(result.contains(otherGroup));
        assertFalse(result.contains(noProjectNoAssignee));
    }

    /** EMPLOYEE: getTaskById trên task assignedTo mình → OK */
    @Test
    void testGetTaskById_EmployeeAssignedToMe_OK() {
        Employee me = employee(10L, "alpha");
        Task task = taskAssignedTo(1L, me);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentEmployee(any(Authentication.class))).thenReturn(me);

        Task result = taskService.getTaskById(1L, employeeAuth());
        assertSame(task, result);
    }

    /** EMPLOYEE: getTaskById trên task cùng project group → OK */
    @Test
    void testGetTaskById_EmployeeSameGroup_OK() {
        Employee me = employee(10L, "alpha");
        Task task = taskWithProjectGroup(1L, "alpha");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentEmployee(any(Authentication.class))).thenReturn(me);

        Task result = taskService.getTaskById(1L, employeeAuth());
        assertSame(task, result);
    }

    /** EMPLOYEE: getTaskById trên task khác group, không phải mình → AccessDenied */
    @Test
    void testGetTaskById_EmployeeOtherGroup_AccessDenied() {
        Employee me = employee(10L, "alpha");
        Task task = taskWithProjectGroup(1L, "beta");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentEmployee(any(Authentication.class))).thenReturn(me);

        assertThrows(AccessDeniedException.class,
                () -> taskService.getTaskById(1L, employeeAuth()));
    }

    /** EMPLOYEE: getTaskById trên task không có project và không có assignee → AccessDenied */
    @Test
    void testGetTaskById_EmployeeOrphanTask_AccessDenied() {
        Employee me = employee(10L, "alpha");
        Task task = new Task();
        task.setTaskId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentEmployee(any(Authentication.class))).thenReturn(me);

        assertThrows(AccessDeniedException.class,
                () -> taskService.getTaskById(1L, employeeAuth()));
    }

    /** EMPLOYEE không có group: chỉ thấy task assignedTo mình (group rỗng không match cái gì) */
    @Test
    void testGetAllTasks_EmployeeWithoutGroup_OnlyAssignedTasks() {
        Employee me = employee(10L, null);

        Task assignedToMe = taskAssignedTo(1L, me);
        Task someProjectWithBlankGroup = taskWithProjectGroup(2L, "");
        Task someProjectWithGroup = taskWithProjectGroup(3L, "alpha");

        when(currentUserService.getCurrentEmployee(any(Authentication.class))).thenReturn(me);
        when(taskRepository.findAll())
                .thenReturn(Arrays.asList(assignedToMe, someProjectWithBlankGroup, someProjectWithGroup));

        List<Task> result = taskService.getAllTasks(employeeAuth());

        assertEquals(1, result.size());
        assertTrue(result.contains(assignedToMe));
    }
}
