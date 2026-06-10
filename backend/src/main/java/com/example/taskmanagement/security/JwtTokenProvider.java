package com.example.taskmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // HS256 yêu cầu key tối thiểu 256-bit = 32 byte (RFC 7518 §3.2). Dưới ngưỡng này
    // jjwt sẽ throw WeakKeyException tại lần parse đầu — gây lỗi 500 mơ hồ thay vì
    // báo cấu hình sai ngay khi boot. Fail-fast tốt hơn.
    private static final int MIN_SECRET_BYTES = 32;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.issuer:taskhub}")
    private String jwtIssuer;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) is required");
        }
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) phải dài ít nhất " + MIN_SECRET_BYTES
                            + " byte cho HS256 (hiện " + keyBytes.length + " byte)");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // Trích role từ authorities ("ROLE_MANAGER" -> "MANAGER") để nhúng vào token
        // làm nguồn chân lý duy nhất cho client (web/mobile) — tránh role desync khi
        // khôi phục session. Authorization phía server vẫn dựa trên SecurityContext.
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse("EMPLOYEE");
        return generateTokenFromUsername(userDetails.getUsername(), role);
    }

    /** Overload không kèm role — mặc định EMPLOYEE (dùng cho test/đường dẫn cũ). */
    public String generateTokenFromUsername(String username) {
        return generateTokenFromUsername(username, "EMPLOYEE");
    }

    public String generateTokenFromUsername(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .issuer(jwtIssuer)
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parse + verify một lần, trả về subject. Trả về null nếu token sai/quá hạn/khác issuer.
     * Gộp validate + getUsername để filter chỉ chịu chi phí parse 1 lần và tránh TOCTOU
     * (token có thể expire giữa 2 lần parse riêng biệt).
     */
    public String parseUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
