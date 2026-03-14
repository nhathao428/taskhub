package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        Employee existing = getEmployeeById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPosition(updated.getPosition());
        existing.setDepartment(updated.getDepartment());
        return employeeRepository.save(existing);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
