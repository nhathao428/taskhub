package com.example.taskmanagement.config;

import com.example.taskmanagement.security.JwtAuthenticationFilter;
import com.example.taskmanagement.security.JwtTokenProvider;
import com.example.taskmanagement.security.RestAuthenticationEntryPoint;
import com.example.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * @EnableMethodSecurity: bật để các annotation @PreAuthorize trên controller thực sự có
 * hiệu lực. Trước đây dự án chỉ phân quyền theo URL ở đây, nên @PreAuthorize viết trong
 * FaceController bị Spring bỏ qua ÂM THẦM — test phát hiện nhân viên thường vẫn gọi được
 * endpoint dành cho quản lý. Giờ có 2 lớp: rule theo URL bên dưới (lớp chính) và
 * annotation ở controller (lớp thứ hai, phòng khi ai đó thêm endpoint mà quên khai báo URL).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CorsConfig corsConfig;
    private final RestAuthenticationEntryPoint authEntryPoint;

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean swaggerEnabled;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, @Lazy UserService userService,
                          CorsConfig corsConfig, RestAuthenticationEntryPoint authEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.corsConfig = corsConfig;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 12: ~150-300ms/hash trên server hiện đại — cao hơn default 10 (4x cost).
        // Đủ chống brute-force GPU trong khi vẫn dưới ngưỡng UX cho login.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection is disabled intentionally: this is a stateless REST API using
            // JWT bearer tokens in the Authorization header, not cookies. CSRF attacks target
            // cookie-based authentication and are not applicable here.
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 401 cho request thiếu/hỏng JWT — Spring Security 6 mặc định trả 403,
            // sai semantics REST (401 = re-auth, 403 = đã auth nhưng cấm).
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/auth/**", "/api/v2/auth/**", "/api/version").permitAll();
                // Swagger paths chỉ permitAll khi SWAGGER_ENABLED=true. Khi disabled,
                // rơi vào .anyRequest().authenticated() → 401 thay vì 500 (Springdoc
                // handler NPE khi bean không tồn tại).
                if (swaggerEnabled) {
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                            "/api-docs/**", "/v3/api-docs/**").permitAll();
                }
                auth

                // Employee self-service endpoints — must come BEFORE the broader rules below
                .requestMatchers(HttpMethod.GET, "/api/employees/me").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tasks/me").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tasks/*/status").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/attendance/me").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/attendance/me/checkin").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/attendance/me/checkout").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                // Nhận diện khuôn mặt. THỨ TỰ QUAN TRỌNG: các rule /me và /capture phải đứng
                // TRƯỚC "/api/face/**", vì Spring Security khớp theo thứ tự khai báo — đặt sau
                // thì rule rộng hơn sẽ nuốt mất.
                // Nhân viên chỉ thao tác trên khuôn mặt của CHÍNH MÌNH (/me).
                .requestMatchers(HttpMethod.GET, "/api/face/me").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/face/me/enroll").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/face/me").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                // Ảnh check-in nghi vấn là dữ liệu sinh trắc học của NGƯỜI KHÁC -> chỉ quản lý.
                .requestMatchers(HttpMethod.GET, "/api/face/capture/**").hasAnyRole("MANAGER", "ADMIN")
                // Xoá đăng ký của nhân viên bất kỳ -> chỉ quản lý.
                .requestMatchers(HttpMethod.DELETE, "/api/face/**").hasAnyRole("MANAGER", "ADMIN")

                // Office locations: mọi user xem được (cần để hiển thị bản đồ check-in);
                // chỉ MANAGER/ADMIN sửa.
                .requestMatchers(HttpMethod.GET, "/api/office-locations/**").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/office-locations/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/office-locations/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/office-locations/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/office-locations/**").hasAnyRole("MANAGER", "ADMIN")

                // Manager review pending attendance
                .requestMatchers(HttpMethod.PATCH, "/api/attendance/*/review").hasAnyRole("MANAGER", "ADMIN")

                // EMPLOYEE: read-only on projects and tasks; everything else requires higher role
                .requestMatchers(HttpMethod.GET, "/api/projects/**").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tasks/**").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                // Write operations on projects/tasks/employees/attendance/suggestions require MANAGER or ADMIN
                .requestMatchers(HttpMethod.POST, "/api/projects/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/projects/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyRole("MANAGER", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/tasks/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tasks/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAnyRole("MANAGER", "ADMIN")

                .requestMatchers("/api/employees/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/attendance/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/suggestions/**").hasAnyRole("MANAGER", "ADMIN")

                // Quản lý tài khoản & phân quyền: chỉ ADMIN
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                .anyRequest().authenticated();
            })
            .headers(headers -> headers
                // Defense-in-depth headers (Security L3 + mitigate H2 XSS exfiltration).
                .frameOptions(frame -> frame.sameOrigin())
                // CSP rất chặt: chỉ cho phép script/style từ same-origin + Swagger CDN (khi enable).
                // Giảm đáng kể bề mặt XSS — kể cả khi attacker inject HTML, browser chặn execute.
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; "
                        + "script-src 'self' 'unsafe-inline'; "
                        + "style-src 'self' 'unsafe-inline'; "
                        + "img-src 'self' data: https:; "
                        + "font-src 'self' data:; "
                        + "connect-src 'self'; "
                        + "frame-ancestors 'self'; "
                        + "base-uri 'self'; "
                        + "form-action 'self'"))
                // HSTS: ép HTTPS 1 năm + preload. Chỉ có hiệu lực khi serve qua HTTPS.
                .httpStrictTransportSecurity(hsts -> hsts
                        .maxAgeInSeconds(31_536_000)
                        .includeSubDomains(true)
                        .preload(true))
                .referrerPolicy(ref -> ref.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentTypeOptions(opts -> {})  // X-Content-Type-Options: nosniff
                // Permissions-Policy: tắt mọi API browser nhạy cảm cho origin của backend
                // (API JSON, không cần camera/mic/geo/USB...). Defense-in-depth khi accident
                // serve HTML từ backend (vd Swagger UI bật) — browser chặn quyền hardware.
                .addHeaderWriter((req, resp) -> resp.setHeader("Permissions-Policy",
                        "accelerometer=(), camera=(), geolocation=(), gyroscope=(), "
                        + "magnetometer=(), microphone=(), payment=(), usb=()"))
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
