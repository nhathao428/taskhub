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
import java.util.HexFormat;
import java.util.Optional;

/**
 * Luồng "quên mật khẩu" bằng mã OTP 6 chữ số gửi qua email (thay cho link đặt lại trước đây).
 *
 * Nguyên tắc bảo mật:
 *  - Anti-enumeration: /forgot-password luôn trả message generic dù email có tồn tại hay không.
 *  - OTP sinh bằng {@link SecureRandom} (không dùng {@code java.util.Random} — không đủ ngẫu
 *    nhiên cho mục đích bảo mật); DB chỉ lưu SHA-256 hash, không lưu OTP thật.
 *  - OTP dùng một lần (used=true sau khi đổi mật khẩu thành công) và hết hạn sau
 *    {@code token-ttl-minutes} — mặc định 10 phút, ngắn hơn link cũ (15 phút) vì không gian chỉ
 *    10^6 khả năng nên dễ bị đoán/brute-force hơn nhiều so với token dài 256-bit.
 *  - Chống brute-force: mỗi lần nhập sai OTP tăng {@code attempts}; vượt quá
 *    {@link #MAX_ATTEMPTS} thì khoá luôn OTP đó (used=true), bắt buộc người dùng yêu cầu mã mới
 *    — 5 lần thử cho 10^6 khả năng là an toàn (không đủ để dò được trong thời hạn 10 phút).
 *  - Mỗi yêu cầu mới vô hiệu hoá các OTP cũ chưa dùng của cùng user (chỉ 1 OTP hiệu lực).
 *
 * Gửi email qua Resend ({@link EmailService}) khi có cấu hình {@code RESEND_API_KEY} — độc
 * lập với cờ {@code app.password-reset.expose-token} (dev vẫn có thể vừa nhận email thật vừa
 * thấy OTP trong response để tiện thao tác end-to-end mà không cần mở hộp thư). Khi CHƯA cấu
 * hình RESEND_API_KEY, hệ thống quay lại hành vi cũ (chỉ log, không gửi gì).
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_BOUND = 1_000_000; // 6 chữ số: 000000..999999

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.token-ttl-minutes:10}")
    private long ttlMinutes;

    @Value("${app.password-reset.expose-token:false}")
    private boolean exposeToken;

    @Value("${app.password-reset.max-attempts:5}")
    private int maxAttempts;

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
     * Phát mã OTP đặt lại cho email (nếu tồn tại user). Luôn trả response generic.
     */
    @Transactional
    public ForgotPasswordResponse requestReset(String email) {
        String generic = "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi mã đặt lại mật khẩu.";
        Optional<User> userOpt = userRepository.findByEmail(email == null ? "" : email.trim());
        if (userOpt.isEmpty()) {
            // Vẫn trả về như khi thành công — không tiết lộ email có tồn tại hay không.
            log.info("Forgot-password cho email không tồn tại (anti-enumeration generic 200)");
            return new ForgotPasswordResponse(generic, null);
        }

        User user = userOpt.get();
        // Mỗi lần yêu cầu → chỉ giữ đúng 1 OTP còn hiệu lực.
        tokenRepository.invalidateAllForUser(user);

        String otp = generateOtp();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(sha256(otp));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        token.setUsed(false);
        token.setAttempts(0);
        tokenRepository.save(token);

        log.info("Đã phát OTP đặt lại mật khẩu (userId={}, ttl={}min)", user.getUserId(), ttlMinutes);

        sendResetEmail(user.getEmail(), otp);

        if (exposeToken) {
            // DEV: vẫn trả OTP thẳng cho client để tiện thao tác end-to-end mà không cần mở
            // hộp thư (dùng song song với gửi email thật ở trên nếu đã cấu hình).
            return new ForgotPasswordResponse(generic, otp);
        }
        return new ForgotPasswordResponse(generic, null);
    }

    /**
     * Gửi email chứa mã OTP đặt lại mật khẩu. Không throw — lỗi gửi email chỉ được log,
     * không được làm hỏng response generic anti-enumeration của /forgot-password.
     */
    private void sendResetEmail(String toEmail, String otp) {
        String subject = "TaskHub — Mã đặt lại mật khẩu của bạn";
        String html = """
                <p>Chào bạn,</p>
                <p>Hệ thống TaskHub nhận được yêu cầu đặt lại mật khẩu cho tài khoản này.</p>
                <p>Mã xác nhận của bạn là:</p>
                <p style="font-size:28px;font-weight:bold;letter-spacing:4px;">%s</p>
                <p>Mã có hiệu lực trong %d phút và chỉ dùng được 1 lần.</p>
                <p>Nếu bạn không yêu cầu việc này, có thể bỏ qua email — mật khẩu hiện tại vẫn giữ nguyên.</p>
                <p>— TaskHub</p>
                """.formatted(otp, ttlMinutes);
        emailService.sendEmail(toEmail, subject, html);
    }

    /**
     * Đổi mật khẩu bằng email + OTP. Ném {@link BusinessException} (message generic) nếu
     * không có OTP đang hiệu lực cho email này, đã dùng, hết hạn, hoặc sai (có đếm số lần sai).
     *
     * QUAN TRỌNG — {@code noRollbackFor}: @Transactional mặc định rollback toàn bộ khi có
     * RuntimeException (BusinessException là RuntimeException), nên nếu không khai báo
     * noRollbackFor thì việc tăng {@code attempts} rồi throw lỗi OTP sai sẽ bị rollback theo —
     * counter không bao giờ tăng thật trong DB, khoá sau N lần sai sẽ KHÔNG BAO GIỜ kích hoạt
     * (đã tự phát hiện bug này lúc test: 6 lần nhập sai liên tiếp vẫn không bị khoá).
     */
    @Transactional(noRollbackFor = BusinessException.class)
    @CacheEvict(value = "user_details", allEntries = true)
    public void resetPassword(String email, String otp, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email == null ? "" : email.trim());
        if (userOpt.isEmpty()) {
            // Không tiết lộ email có tồn tại hay không — cùng message với các nhánh lỗi khác.
            throw invalidOtpException();
        }
        User user = userOpt.get();

        PasswordResetToken token = tokenRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(PasswordResetService::invalidOtpException);

        if (token.isExpired()) {
            throw invalidOtpException();
        }
        if (token.getAttempts() >= maxAttempts) {
            // Đã vượt số lần thử cho phép — khoá luôn, không cho thử tiếp dù OTP đúng hay sai.
            token.setUsed(true);
            tokenRepository.save(token);
            throw tooManyAttemptsException();
        }

        if (!sha256(otp == null ? "" : otp.trim()).equals(token.getTokenHash())) {
            token.setAttempts(token.getAttempts() + 1);
            tokenRepository.save(token);
            if (token.getAttempts() >= maxAttempts) {
                token.setUsed(true);
                tokenRepository.save(token);
                throw tooManyAttemptsException();
            }
            throw invalidOtpException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Đặt lại mật khẩu thành công (userId={})", user.getUserId());
    }

    private static BusinessException invalidOtpException() {
        return new BusinessException("Mã OTP không đúng hoặc đã hết hạn. Vui lòng yêu cầu mã mới.");
    }

    private static BusinessException tooManyAttemptsException() {
        return new BusinessException("Bạn đã nhập sai mã quá nhiều lần. Vui lòng yêu cầu mã đặt lại mật khẩu mới.");
    }

    /** OTP 6 chữ số, sinh bằng SecureRandom (không dùng Random thường) — giữ số 0 ở đầu nếu có. */
    private static String generateOtp() {
        int n = SECURE_RANDOM.nextInt(OTP_BOUND);
        return String.format("%06d", n);
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
