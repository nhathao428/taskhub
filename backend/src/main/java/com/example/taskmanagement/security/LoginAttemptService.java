package com.example.taskmanagement.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-account login lockout chống brute-force phân tán (Security M4).
 * Đếm số lần fail login theo username trong cửa sổ trượt; lock 15 phút sau ngưỡng.
 *
 * Store in-memory (đủ cho 1 instance). Multi-instance cần chuyển sang Redis SETEX.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    @Value("${app.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.login.lockout-minutes:15}")
    private long lockoutMinutes;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String identifier) {
        Attempt attempt = attempts.get(normalize(identifier));
        if (attempt == null) return false;
        long now = System.currentTimeMillis();
        if (attempt.lockedUntilMs > now) return true;
        // Lock đã hết hạn — clear để cho thử lại.
        if (attempt.lockedUntilMs > 0) {
            attempts.remove(normalize(identifier));
        }
        return false;
    }

    public void recordFailure(String identifier) {
        String key = normalize(identifier);
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());
        int count = attempt.failureCount.incrementAndGet();
        if (count >= maxAttempts) {
            attempt.lockedUntilMs = System.currentTimeMillis() + lockoutMinutes * 60_000L;
            log.warn("Account locked: identifier_hash={} (failures={}, lockout={}min)",
                    hashForLog(key), count, lockoutMinutes);
        }
    }

    public void recordSuccess(String identifier) {
        attempts.remove(normalize(identifier));
    }

    private String normalize(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase();
    }

    // Log hash thay vì raw username (Security M3 — không leak PII vào logs).
    private String hashForLog(String key) {
        return Integer.toHexString(key.hashCode());
    }

    private static final class Attempt {
        final AtomicInteger failureCount = new AtomicInteger(0);
        volatile long lockedUntilMs = 0;
    }
}
