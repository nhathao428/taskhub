package com.example.taskmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Khuôn mặt đã đăng ký (enroll) của một nhân viên, dùng cho check-in bằng nhận diện.
 *
 * KHÔNG LƯU ẢNH. Chỉ lưu embedding — vector 512 chiều do model FaceNet sinh ra, đã được
 * mã hoá AES-256-GCM (xem BiometricCrypto). Ảnh gốc bị huỷ ngay sau khi trích xuất xong,
 * không ghi ra đĩa, không ghi vào DB.
 *
 * Vì sao mã hoá thay vì hash: xác thực khuôn mặt cần so khoảng cách giữa 2 vector, nên
 * bắt buộc phải đọc lại được giá trị gốc — không dùng hash một chiều như mật khẩu được.
 *
 * Mỗi nhân viên chỉ có 1 bản ghi (đăng ký lại sẽ ghi đè), giữ đơn giản cho phạm vi đồ án.
 */
@Entity
@Table(name = "employee_faces")
@Getter
@Setter
@ToString(exclude = {"employee", "embeddingEncrypted"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmployeeFace implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_face_id")
    private Long employeeFaceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Employee employee;

    /**
     * Embedding đã mã hoá, dạng base64 của [IV 12 byte][ciphertext + tag].
     * @JsonIgnore: tuyệt đối không để lọt ra API response dưới bất kỳ dạng nào.
     */
    @JsonIgnore
    @Column(name = "embedding_encrypted", nullable = false, columnDefinition = "TEXT")
    private String embeddingEncrypted;

    /** Số ảnh dùng để tính embedding trung bình lúc đăng ký (càng nhiều càng ổn định). */
    @Column(name = "sample_count", nullable = false)
    private int sampleCount = 1;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
