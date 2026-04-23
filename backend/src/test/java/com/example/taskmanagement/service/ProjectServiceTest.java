package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ProjectService projectService;

    private Authentication managerAuth() {
        return new UsernamePasswordAuthenticationToken(
                "manager1", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication employeeAuth(String username) {
        return new UsernamePasswordAuthenticationToken(
                username, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    /** MANAGER getAllProjects → trả về tất cả projects */
    @Test
    void testGetAllProjects_Manager_ReturnsAll() {
        Project p1 = new Project(); p1.setName("P1"); p1.setGroup("GroupA");
        Project p2 = new Project(); p2.setName("P2"); p2.setGroup("GroupB");
        when(projectRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Project> result = projectService.getAllProjects(managerAuth());

        assertEquals(2, result.size());
        verify(projectRepository).findAll();
        verify(projectRepository, never()).findByGroup(any());
    }

    /** ADMIN getAllProjects → trả về tất cả projects */
    @Test
    void testGetAllProjects_Admin_ReturnsAll() {
        Project p1 = new Project(); p1.setName("P1"); p1.setGroup("GroupA");
        when(projectRepository.findAll()).thenReturn(Collections.singletonList(p1));

        List<Project> result = projectService.getAllProjects(adminAuth());

        assertEquals(1, result.size());
        verify(projectRepository).findAll();
    }

    /** EMPLOYEE getAllProjects → chỉ trả về projects cùng group */
    @Test
    void testGetAllProjects_Employee_FiltersToOwnGroup() {
        String username = "emp1";
        User user = new User(); user.setUsername(username); user.setRole("EMPLOYEE");
        Employee employee = new Employee(); employee.setGroup("GroupA");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));

        Project p1 = new Project(); p1.setName("P1"); p1.setGroup("GroupA");
        when(projectRepository.findByGroup("GroupA")).thenReturn(Collections.singletonList(p1));

        List<Project> result = projectService.getAllProjects(employeeAuth(username));

        assertEquals(1, result.size());
        assertEquals("GroupA", result.get(0).getGroup());
        verify(projectRepository).findByGroup("GroupA");
        verify(projectRepository, never()).findAll();
    }

    /** EMPLOYEE với no group → trả về danh sách rỗng */
    @Test
    void testGetAllProjects_Employee_NoGroup_ReturnsEmpty() {
        String username = "emp2";
        User user = new User(); user.setUsername(username); user.setRole("EMPLOYEE");
        Employee employee = new Employee(); employee.setGroup(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));

        List<Project> result = projectService.getAllProjects(employeeAuth(username));

        assertTrue(result.isEmpty());
        verify(projectRepository, never()).findByGroup(any());
        verify(projectRepository, never()).findAll();
    }

    /** EMPLOYEE getProjectById với project cùng group → trả về project */
    @Test
    void testGetProjectById_Employee_SameGroup_Returns() {
        String username = "emp1";
        User user = new User(); user.setUsername(username);
        Employee employee = new Employee(); employee.setGroup("GroupA");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));

        Project p = new Project(); p.setName("P1"); p.setGroup("GroupA");
        when(projectRepository.findByProjectIdAndGroup(1L, "GroupA")).thenReturn(Optional.of(p));

        Project result = projectService.getProjectById(1L, employeeAuth(username));

        assertNotNull(result);
        assertEquals("P1", result.getName());
    }

    /** EMPLOYEE getProjectById với project khác group → ném ResourceNotFoundException */
    @Test
    void testGetProjectById_Employee_DifferentGroup_ThrowsNotFound() {
        String username = "emp1";
        User user = new User(); user.setUsername(username);
        Employee employee = new Employee(); employee.setGroup("GroupA");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));
        when(projectRepository.findByProjectIdAndGroup(99L, "GroupA")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.getProjectById(99L, employeeAuth(username)));
    }

    /** EMPLOYEE getProjectById khi chưa có group → ném AccessDeniedException */
    @Test
    void testGetProjectById_Employee_NoGroup_ThrowsAccessDenied() {
        String username = "emp3";
        User user = new User(); user.setUsername(username);
        Employee employee = new Employee(); employee.setGroup(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));

        assertThrows(AccessDeniedException.class,
                () -> projectService.getProjectById(1L, employeeAuth(username)));
    }
}
