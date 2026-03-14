package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectProjectId(Long projectId);
    List<Task> findByAssignedToEmployeeId(Long employeeId);
    List<Task> findByStatus(String status);
    List<Task> findByAssignedToEmployeeIdAndStatusIn(Long employeeId, List<String> statuses);
}
