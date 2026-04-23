package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
    List<Employee> findByPosition(String position);
    Optional<Employee> findByUser(User user);
}
