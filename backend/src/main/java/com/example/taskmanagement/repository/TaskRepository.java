package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Task queries eagerly tải sẵn `project` và `assignedTo` qua @EntityGraph để
 * tránh N+1 + cho phép Jackson serialize an toàn khi spring.jpa.open-in-view=false.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Override
    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findAll();

    @Override
    @EntityGraph(attributePaths = {"project", "assignedTo"})
    Optional<Task> findById(Long id);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findByProjectProjectId(Long projectId);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findByAssignedToEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findByStatus(String status);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findByAssignedToEmployeeIdAndStatusIn(Long employeeId, List<String> statuses);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findByAssignedToEmployeeIdInAndStatusIn(List<Long> employeeIds, List<String> statuses);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Task> findByAssignedToEmployeeIdIn(List<Long> employeeIds);
}
