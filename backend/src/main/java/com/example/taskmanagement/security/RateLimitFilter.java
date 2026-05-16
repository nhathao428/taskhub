package com.example.taskmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chống spam: giới hạn số request mỗi IP trong một cửa sổ 1 phút cho các endpoint
 * dễ bị lạm dụng — đăng nhập/đăng ký (chống brute-force) và gợi ý AI (bảo vệ quota Gemini).
 * Đếm theo cửa sổ cố định, lưu trong bộ nhớ — đủ cho một instance; nếu chạy nhiều
 * instance cần chuyển sang Redis. Chạy trước Spring Security để chặn sớm.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long WINDOW_MS = 60_000;

    @Value("${app.ratelimit.auth:20}")
    private int authLimit;

    @Value("${app.ratelimit.ai:10}")
    private int aiLimit;

    private final ObjectMapper objectMapper;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String bucket = bucketFor(request.getRequestURI());
        if (bucket != null) {
            int limit = "auth".equals(bucket) ? authLimit : aiLimit;
            String key = clientIp(request) + "|" + bucket;
            if (!allow(key, limit)) {
                log.warn("Rate limit vượt ngưỡng: key={}, limit={}/phút", key, limit);
                writeTooManyRequests(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /** Trả về tên nhóm giới hạn, hoặc null nếu endpoint không bị giới hạn. */
    private String bucketFor(String uri) {
        if (uri.startsWith("/api/auth/")) return "auth";
        if (uri.startsWith("/api/suggestions/")) return "ai";
        return null;
    }

    private boolean allow(String key, int limit) {
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, current) ->
                (current == null || now - current.windowStart >= WINDOW_MS)
                        ? new Window(now)
                        : current);
        return window.count.incrementAndGet() <= limit;
    }

    /** Lấy IP thật, ưu tiên X-Forwarded-For khi chạy sau reverse proxy (Caddy). */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "message", "Bạn gửi quá nhiều yêu cầu, vui lòng thử lại sau ít phút.",
                "timestamp", LocalDateTime.now().toString()));
    }

    private static final class Window {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
