package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeEmployeeId(Long employeeId);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByEmployeeEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<Attendance> findByEmployeeEmployeeIdInAndDateBetween(List<Long> employeeIds, LocalDate start, LocalDate end);
}
