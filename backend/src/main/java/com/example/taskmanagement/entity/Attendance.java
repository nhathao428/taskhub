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

    /*
     * ----------- Geofence fields (chấm công xác thực qua GPS) -----------
     * QUAN TRỌNG (audit bảo mật 8/2026): 4 cột toạ độ dưới đây KHÔNG còn được service ghi
     * giá trị nữa — luôn null từ nay. Lý do: lịch sử vị trí GPS chi tiết, giữ vĩnh viễn,
     * mọi Manager đều xem được toàn bộ, là dữ liệu nhạy cảm nhất hệ thống nếu DB rò rỉ,
     * trong khi nghiệp vụ chỉ cần biết trong/ngoài bán kính (xem checkInDistanceMeters +
     * reviewStatus), không cần toạ độ chính xác. Giữ cột lại trong schema (không migration
     * xoá) để tránh rủi ro không cần thiết — xem AttendanceService.applyLocation().
     */

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

    /*
     * ----------- Nhận diện khuôn mặt (đồ án chuyên ngành 8/2026) -----------
     * Chỉ lưu KẾT QUẢ xác thực, không lưu ảnh cũng không lưu embedding của lần check-in
     * này — ảnh bị huỷ ngay sau khi so khớp xong. Embedding đăng ký của nhân viên nằm ở
     * bảng riêng employee_faces, đã mã hoá (xem EmployeeFace + BiometricCrypto).
     * null = lần check-in đó không dùng nhận diện khuôn mặt (chỉ GPS như trước).
     */

    /** true nếu khuôn mặt khớp với người đang đăng nhập, false nếu không khớp. */
    @Column(name = "face_verified")
    private Boolean faceVerified;

    /** Độ tương đồng cosine với embedding đã đăng ký (0-1). Dùng để đối chiếu khi review. */
    @Column(name = "face_similarity")
    private Float faceSimilarity;

    /** true nếu qua được kiểm tra chống giả mạo (phát hiện chớp mắt). */
    @Column(name = "liveness_passed")
    private Boolean livenessPassed;
}
