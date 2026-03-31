package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.CreateEmployeeRequest;
import com.example.taskmanagement.dto.UpdateEmployeeRequest;
import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getAllEmployees()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getEmployeeById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(employeeService.createEmployee(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.updateEmployee(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
