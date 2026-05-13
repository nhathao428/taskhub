package com.example.taskmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Một văn phòng / chi nhánh cho phép nhân viên chấm công.
 * Chấm công nằm trong bán kính (radiusMeters) tính từ (latitude, longitude)
 * sẽ được auto-approved; nằm ngoài sẽ chuyển sang PENDING_REVIEW.
 */
@Entity
@Table(name = "office_locations")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OfficeLocation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status { ACTIVE, INACTIVE }

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /** Bán kính geofence tính bằng mét. Mặc định 100m. */
    @Column(name = "radius_meters", nullable = false)
    private Integer radiusMeters = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (radiusMeters == null) radiusMeters = 100;
        if (status == null) status = Status.ACTIVE;
    }
}
