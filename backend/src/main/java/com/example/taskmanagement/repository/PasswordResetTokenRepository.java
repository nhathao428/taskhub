package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.PasswordResetToken;
import com.example.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * OTP không còn duy nhất giữa các user (10^6 khả năng, có thể trùng ngẫu nhiên) nên verify
     * tra theo (user, used=false) thay vì tra trực tiếp theo token_hash. Nhờ
     * {@link #invalidateAllForUser} chạy trước mỗi lần phát OTP mới, tại một thời điểm mỗi
     * user chỉ có tối đa 1 bản ghi chưa dùng — lấy bản mới nhất cho chắc (phòng race condition).
     */
    Optional<PasswordResetToken> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    /**
     * Vô hiệu hoá mọi token chưa dùng của một user — gọi trước khi phát OTP mới
     * để mỗi lần "quên mật khẩu" chỉ còn đúng 1 mã còn hiệu lực.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user = :user AND t.used = false")
    void invalidateAllForUser(@Param("user") User user);
}
