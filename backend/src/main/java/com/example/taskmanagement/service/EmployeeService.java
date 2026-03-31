package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateEmployeeRequest;
import com.example.taskmanagement.dto.UpdateEmployeeRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Cacheable("employees")
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Cacheable(value = "employees", key = "#id")
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @CacheEvict(value = "employees", allEntries = true)
    public Employee createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setPosition(request.position());
        employee.setDepartment(request.department());
        return employeeRepository.save(employee);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public Employee updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        if (request.firstName() != null) existing.setFirstName(request.firstName());
        if (request.lastName() != null) existing.setLastName(request.lastName());
        if (request.position() != null) existing.setPosition(request.position());
        if (request.department() != null) existing.setDepartment(request.department());
        return employeeRepository.save(existing);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
