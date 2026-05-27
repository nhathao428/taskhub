package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Attendance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Attendance queries eagerly tải `employee` và `checkInOffice` để JSON serialization
 * không phụ thuộc vào Open Session In View (spring.jpa.open-in-view=false).
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Override
    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    List<Attendance> findAll();

    @Override
    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    Optional<Attendance> findById(Long id);

    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    List<Attendance> findByEmployeeEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    List<Attendance> findByDate(LocalDate date);

    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    List<Attendance> findByEmployeeEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    List<Attendance> findByEmployeeEmployeeIdInAndDateBetween(List<Long> employeeIds, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    List<Attendance> findByEmployeeEmployeeIdOrderByDateDescCheckInDesc(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "checkInOffice"})
    Optional<Attendance> findFirstByEmployeeEmployeeIdAndDateAndCheckOutIsNullOrderByCheckInDesc(Long employeeId, LocalDate date);
}
