package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.AttendanceFaceCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AttendanceFaceCaptureRepository extends JpaRepository<AttendanceFaceCapture, Long> {

    Optional<AttendanceFaceCapture> findByAttendanceAttendanceId(Long attendanceId);

    /** Job dọn dẹp: xoá ảnh quá hạn lưu trữ. */
    @Modifying
    @Query("DELETE FROM AttendanceFaceCapture c WHERE c.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    long countByExpiresAtBefore(LocalDateTime time);
}
