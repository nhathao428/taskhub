package com.example.taskmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@ToString(exclude = {"employee", "checkInOffice"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Attendance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum ReviewStatus { APPROVED, PENDING_REVIEW, REJECTED }

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Employee employee;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "check_in", nullable = false)
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    /* ----------- Geofence fields (chấm công xác thực qua GPS) ----------- */

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lng")
    private Double checkInLng;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_office_id")
    private OfficeLocation checkInOffice;

    /** Khoảng cách từ vị trí check-in đến tâm office (mét, lúc check-in). */
    @Column(name = "check_in_distance_m")
    private Integer checkInDistanceMeters;

    /** APPROVED khi nằm trong radius / PENDING_REVIEW khi ngoài / REJECTED khi quản lý từ chối. */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    private ReviewStatus reviewStatus = ReviewStatus.APPROVED;

    /** Cờ cho biết client báo GPS mock (mobile thường set true nếu phát hiện fake location). */
    @Column(name = "is_mocked")
    private Boolean isMocked = false;
}
