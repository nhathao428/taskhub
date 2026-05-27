package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateEmployeeRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private EmployeeService employeeService;

    /** getAllEmployees → trả về danh sách đúng size */
    @Test
    void testGetAllEmployees() {
        Employee emp1 = new Employee();
        emp1.setFirstName("Alice");
        Employee emp2 = new Employee();
        emp2.setFirstName("Bob");

        when(employeeRepository.findAll()).thenReturn(Arrays.asList(emp1, emp2));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        verify(employeeRepository).findAll();
    }

    /** getEmployeeById với id tồn tại → trả về Employee đúng */
    @Test
    void testGetEmployeeById_Found() {
        Employee employee = new Employee();
        employee.setFirstName("Alice");
        employee.setLastName("Smith");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals("Alice", result.getFirstName());
    }

    /** getEmployeeById với id không tồn tại → ném ResourceNotFoundException */
    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(99L));
    }

    /** createEmployee → save() được gọi với đúng fields */
    @Test
    void testCreateEmployee() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "John", "Doe", "Developer", "Engineering", null, null, null);

        Employee saved = new Employee();
        saved.setFirstName("John");
        saved.setLastName("Doe");
        saved.setPosition("Developer");
        saved.setDepartment("Engineering");

        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        Employee result = employeeService.createEmployee(request);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Developer", result.getPosition());
        assertEquals("Engineering", result.getDepartment());
        verify(employeeRepository).save(any(Employee.class));
    }

    /** deleteEmployee → deleteById được gọi với đúng id */
    @Test
    void testDeleteEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }
}
