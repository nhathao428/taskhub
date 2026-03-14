package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByEmployeeEmployeeId(Long employeeId);
    List<Skill> findBySkillName(String skillName);
}
