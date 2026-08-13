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
 * dễ bị lạm dụng — đăng nhập/đăng ký (chống brute-force), gợi ý AI (bảo vệ quota Gemini),
 * và employees/attendance (chặn kéo dữ liệu hàng loạt — attendance chứa toạ độ GPS chấm
 * công — nếu 1 tài khoản Manager bị lộ/chiếm; audit tháng 8/2026 phát hiện 2 nhóm endpoint
 * này trước đó hoàn toàn không có rate limit).
 * Đếm theo cửa sổ cố định, lưu trong bộ nhớ — đủ cho một instance; nếu chạy nhiều
 * instance cần chuyển sang Redis. Chạy trước Spring Security để chặn sớm.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long WINDOW_MS = 60_000;

    @Value("${app.ratelimit.auth:20}")
    private int authLimit;

    @Value("${app.ratelimit.ai:10}")
    private int aiLimit;

    // Chặn kéo dữ liệu hàng loạt (employees + attendance chứa toạ độ GPS chấm công) nếu 1
    // tài khoản Manager bị lộ/chiếm — các endpoint CRUD thường không có rate limit trước đây.
    @Value("${app.ratelimit.employees:40}")
    private int employeesLimit;

    // Chỉ bật khi chạy sau reverse proxy tin cậy (Caddy/Nginx). Nếu false (default),
    // dùng remoteAddr — tránh bị spoof X-Forwarded-For để vượt rate limit.
    @Value("${app.ratelimit.trust-forwarded-header:false}")
    private boolean trustForwardedHeader;

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
            int limit = limitFor(bucket);
            String key = clientIp(request) + "|" + bucket;
            if (!allow(key, limit)) {
                log.warn("Rate limit vượt ngưỡng: key={}, limit={}/phút", key, limit);
                writeTooManyRequests(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Trả về tên nhóm giới hạn, hoặc null nếu endpoint không bị giới hạn.
     * Normalize prefix /api/v{N}/ về /api/ để áp cùng quota cho mọi phiên bản —
     * tránh attacker bypass rate limit bằng cách đổi sang /api/v2/auth/...
     * (ApiVersionAliasFilter đã rewrite /api/v1/, nhưng v2+ phải tự handle).
     */
    private String bucketFor(String uri) {
        String normalized = uri.replaceFirst("^/api/v\\d+/", "/api/");
        if (normalized.startsWith("/api/auth/")) return "auth";
        if (normalized.startsWith("/api/suggestions/")) return "ai";
        // /api/attendance/me/checkin và /me/checkout KHÔNG tính vào bucket "employees":
        // đây là hành động tự phục vụ của 1 nhân viên (ghi 1 dòng, tự giới hạn — không thể
        // dùng để kéo dữ liệu hàng loạt), khác hẳn rủi ro mà bucket này nhắm tới (đọc/liệt
        // kê hàng loạt). Test thực tế phát hiện: nếu dùng chung bucket, 1 IP có nhiều nhân
        // viên chấm công cùng lúc (vd cùng WiFi văn phòng) hoặc manager mở dashboard nhân
        // viên trước đó có thể vô tình làm nhân viên khác check-in bị chặn 429 oan.
        if (normalized.equals("/api/attendance/me/checkin") || normalized.equals("/api/attendance/me/checkout")) {
            return null;
        }
        // "equals" bắt buộc phải kiểm tra riêng — GET /api/employees (list, không có gì
        // theo sau) không khớp startsWith("/api/employees/") vì thiếu dấu "/" cuối. Bug
        // này đã khiến rate limit không áp dụng chút nào lúc test lần đầu.
        if (normalized.equals("/api/employees") || normalized.startsWith("/api/employees/")) return "employees";
        if (normalized.equals("/api/attendance") || normalized.startsWith("/api/attendance/")) return "employees";
        return null;
    }

    private int limitFor(String bucket) {
        return switch (bucket) {
            case "auth" -> authLimit;
            case "ai" -> aiLimit;
            case "employees" -> employeesLimit;
            default -> Integer.MAX_VALUE;
        };
    }

    private boolean allow(String key, int limit) {
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, current) ->
                (current == null || now - current.windowStart >= WINDOW_MS)
                        ? new Window(now)
                        : current);
        return window.count.incrementAndGet() <= limit;
    }

    /**
     * Lấy IP thật. Chỉ đọc X-Forwarded-For khi `trustForwardedHeader=true` (sau reverse
     * proxy tin cậy đã strip header từ client). Mặc định false để chống spoof.
     */
    private String clientIp(HttpServletRequest request) {
        if (trustForwardedHeader) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
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
