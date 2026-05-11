package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateEmployeeRequest;
import com.example.taskmanagement.dto.UpdateEmployeeRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public EmployeeService(EmployeeRepository employeeRepository, CurrentUserService currentUserService) {
        this.employeeRepository = employeeRepository;
        this.currentUserService = currentUserService;
    }

    @Cacheable("employees")
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Cacheable(value = "employees", key = "#id")
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public Employee createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setPosition(request.position());
        employee.setDepartment(request.department());
        employee.setGroup(request.group());
        employee.setSkills(request.skills());
        return employeeRepository.save(employee);
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public Employee updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        if (request.firstName() != null) existing.setFirstName(request.firstName());
        if (request.lastName() != null) existing.setLastName(request.lastName());
        if (request.position() != null) existing.setPosition(request.position());
        if (request.department() != null) existing.setDepartment(request.department());
        if (request.group() != null) existing.setGroup(request.group());
        if (request.skills() != null) existing.setSkills(request.skills());
        return employeeRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        employeeRepository.deleteById(id);
    }

    /** Returns the Employee profile linked to the currently authenticated user. */
    @Transactional(readOnly = true)
    public Employee getMyProfile(Authentication auth) {
        return currentUserService.getCurrentEmployee(auth);
    }
}
