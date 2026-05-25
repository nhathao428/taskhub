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
 * Seeds a MANAGER account on startup if none exists yet.
 * Default credentials: manager1 / Manager@12345
 * Configure via env vars MANAGER_USERNAME / MANAGER_EMAIL / MANAGER_PASSWORD.
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
        String managerEmail = System.getenv("MANAGER_EMAIL");
        if (managerEmail == null) {
            managerEmail = "manager1@example.com";
        }

        if (userRepository.existsByEmail(managerEmail)) {
            log.info("Manager account already exists — skipping seed.");
            return;
        }

        String managerUsername = System.getenv("MANAGER_USERNAME");
        if (managerUsername == null) {
            managerUsername = "manager1";
        }

        String managerPassword = System.getenv("MANAGER_PASSWORD");
        if (managerPassword == null) {
            managerPassword = "Manager@12345";
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
