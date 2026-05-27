package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Employee> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Employee> findByDepartment(String department);

    @EntityGraph(attributePaths = {"user"})
    List<Employee> findByPosition(String position);

    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findByUser(User user);
}
