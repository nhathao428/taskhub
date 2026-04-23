package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatus(String status);
    List<Project> findByGroup(String group);
    Optional<Project> findByProjectIdAndGroup(Long projectId, String group);
}
