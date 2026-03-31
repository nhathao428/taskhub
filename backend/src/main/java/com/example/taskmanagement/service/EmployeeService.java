package com.example.taskmanagement.service;

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
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    @CacheEvict(value = "employees", allEntries = true)
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPosition(updated.getPosition());
        existing.setDepartment(updated.getDepartment());
        return employeeRepository.save(existing);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
