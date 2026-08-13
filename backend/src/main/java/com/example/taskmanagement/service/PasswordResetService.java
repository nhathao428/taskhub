package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.ForgotPasswordResponse;
import com.example.taskmanagement.entity.PasswordResetToken;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.BusinessException;
import com.example.taskmanagement.repository.PasswordResetTokenRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Luồng "quên mật khẩu" (token-based).
 *
 * Nguyên tắc bảo mật:
 *  - Anti-enumeration: /forgot-password luôn trả message generic dù email có tồn tại hay không.
 *  - Token thật (random 256-bit) chỉ gửi cho người dùng; DB chỉ lưu SHA-256 hash.
 *  - Token dùng một lần (used=true sau khi đổi) và hết hạn sau {@code token-ttl-minutes}.
 *  - Mỗi yêu cầu mới vô hiệu hoá các token cũ chưa dùng của cùng user.
 *
 * Gửi email qua Resend ({@link EmailService}) khi có cấu hình {@code RESEND_API_KEY} — độc
 * lập với cờ {@code app.password-reset.expose-token} (dev vẫn có thể vừa nhận email thật vừa
 * thấy token/link trong response để tiện thao tác end-to-end mà không cần mở hộp thư). Khi
 * CHƯA cấu hình RESEND_API_KEY, hệ thống quay lại hành vi cũ (chỉ log, không gửi gì).
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.token-ttl-minutes:15}")
    private long ttlMinutes;

    @Value("${app.password-reset.expose-token:false}")
    private boolean exposeToken;

    @Value("${app.password-reset.reset-url-base:http://localhost:5173/reset-password}")
    private String resetUrlBase;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Phát token đặt lại cho email (nếu tồn tại user). Luôn trả response generic.
     */
    @Transactional
    public ForgotPasswordResponse requestReset(String email) {
        String generic = "Nếu email tồn tại trong hệ thống, chúng tôi đã tạo liên kết đặt lại mật khẩu.";
        Optional<User> userOpt = userRepository.findByEmail(email == null ? "" : email.trim());
        if (userOpt.isEmpty()) {
            // Vẫn trả về như khi thành công — không tiết lộ email có tồn tại hay không.
            log.info("Forgot-password cho email không tồn tại (anti-enumeration generic 200)");
            return new ForgotPasswordResponse(generic, null, null);
        }

        User user = userOpt.get();
        // Mỗi lần yêu cầu → chỉ giữ đúng 1 token còn hiệu lực.
        tokenRepository.invalidateAllForUser(user);

        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(sha256(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        token.setUsed(false);
        tokenRepository.save(token);

        log.info("Đã phát token đặt lại mật khẩu (userId={}, ttl={}min)", user.getUserId(), ttlMinutes);

        String link = resetUrlBase + "?token=" + rawToken;
        sendResetEmail(user.getEmail(), link);

        if (exposeToken) {
            // DEV: vẫn trả token + link thẳng cho client để tiện thao tác end-to-end mà
            // không cần mở hộp thư (dùng song song với gửi email thật ở trên nếu đã cấu hình).
            return new ForgotPasswordResponse(generic, rawToken, link);
        }
        return new ForgotPasswordResponse(generic, null, null);
    }

    /**
     * Gửi email chứa liên kết đặt lại mật khẩu. Không throw — lỗi gửi email chỉ được log,
     * không được làm hỏng response generic anti-enumeration của /forgot-password.
     */
    private void sendResetEmail(String toEmail, String resetLink) {
        String subject = "TaskHub — Yêu cầu đặt lại mật khẩu";
        String html = """
                <p>Chào bạn,</p>
                <p>Hệ thống TaskHub nhận được yêu cầu đặt lại mật khẩu cho tài khoản này.</p>
                <p><a href="%s">Bấm vào đây để đặt lại mật khẩu</a> (liên kết hết hạn sau %d phút).</p>
                <p>Nếu bạn không yêu cầu việc này, có thể bỏ qua email — mật khẩu hiện tại vẫn giữ nguyên.</p>
                <p>— TaskHub</p>
                """.formatted(resetLink, ttlMinutes);
        emailService.sendEmail(toEmail, subject, html);
    }

    /**
     * Đổi mật khẩu bằng token. Ném {@link BusinessException} (message generic) nếu
     * token không hợp lệ / đã dùng / hết hạn.
     */
    @Transactional
    @CacheEvict(value = "user_details", allEntries = true)
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(PasswordResetService::invalidTokenException);

        if (token.isUsed() || token.isExpired()) {
            throw invalidTokenException();
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Đặt lại mật khẩu thành công (userId={})", user.getUserId());
    }

    private static BusinessException invalidTokenException() {
        return new BusinessException("Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.");
    }

    /** Token ngẫu nhiên 256-bit, mã hoá Base64 URL-safe (không padding) → an toàn đặt trên URL. */
    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 là thuật toán bắt buộc của JVM — không bao giờ xảy ra.
            throw new IllegalStateException("SHA-256 không khả dụng", e);
        }
    }
}
