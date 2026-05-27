package com.example.taskmanagement.config;

import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a MANAGER account on startup, only when {@code MANAGER_PASSWORD} is set.
 *
 * Không có default password — nếu thiếu MANAGER_PASSWORD thì bỏ qua bước seed
 * (tránh tạo tài khoản backdoor với mật khẩu yếu, ai cũng đoán được).
 * Cấu hình thêm qua env: MANAGER_USERNAME (mặc định "manager1") và
 * MANAGER_EMAIL (mặc định "manager1@example.com").
 *
 * Khi cần manager mới ở prod: ADMIN có thể đăng ký user thường rồi nâng vai trò
 * qua PATCH /api/users/{id}/role.
 */
@Component
public class ManagerSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ManagerSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String managerPassword = System.getenv("MANAGER_PASSWORD");
        if (managerPassword == null || managerPassword.isBlank()) {
            log.info("MANAGER_PASSWORD not set — skipping manager seed. "
                    + "Set MANAGER_PASSWORD env var to seed a manager account, "
                    + "or promote an EMPLOYEE via PATCH /api/users/{id}/role.");
            return;
        }

        String managerEmail = System.getenv("MANAGER_EMAIL");
        if (managerEmail == null || managerEmail.isBlank()) {
            managerEmail = "manager1@example.com";
        }

        if (userRepository.existsByEmail(managerEmail)) {
            log.info("Manager account already exists — skipping seed.");
            return;
        }

        String managerUsername = System.getenv("MANAGER_USERNAME");
        if (managerUsername == null || managerUsername.isBlank()) {
            managerUsername = "manager1";
        }

        User manager = new User();
        manager.setUsername(managerUsername);
        manager.setEmail(managerEmail);
        manager.setPassword(passwordEncoder.encode(managerPassword));
        manager.setRole("MANAGER");
        userRepository.save(manager);
        log.info("Manager account seeded: username={}, email={}", managerUsername, managerEmail);
    }
}
