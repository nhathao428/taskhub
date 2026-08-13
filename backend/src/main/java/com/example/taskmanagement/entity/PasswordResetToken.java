package com.example.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Mã OTP đặt lại mật khẩu (luồng "quên mật khẩu").
 *
 * Bảo mật: cột {@code token_hash} CHỈ lưu SHA-256 của mã OTP 6 chữ số thật — giống nguyên
 * tắc không lưu mật khẩu plaintext. KHÔNG unique giữa các user: OTP chỉ có 10^6 khả năng nên
 * hai user khác nhau hoàn toàn có thể ngẫu nhiên trùng mã cùng lúc — tra cứu lúc verify luôn
 * đi theo (user, used=false), không tra trực tiếp theo token_hash.
 *
 * Chống brute-force 6 số: {@code attempts} đếm số lần nhập sai; verify tăng dần và khoá
 * (used=true) khi vượt ngưỡng cho phép — xem {@code PasswordResetService.MAX_ATTEMPTS}.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    // Không cascade từ User → token; xoá user thì DB lo (ON DELETE CASCADE ở migration).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now());
    }
}
