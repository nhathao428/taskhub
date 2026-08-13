package com.example.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Gửi email giao dịch (transactional email) qua Resend API (https://resend.com).
 *
 * Free tier chưa verify domain riêng: BẮT BUỘC {@code from} phải là địa chỉ
 * {@code onboarding@resend.dev}, và Resend CHỈ cho gửi tới đúng email đã dùng để đăng ký
 * tài khoản Resend (giới hạn sandbox chống spam) — gửi tới email khác sẽ bị Resend trả lỗi.
 * Muốn gửi tới bất kỳ người dùng nào phải verify domain riêng tại resend.com/domains rồi đổi
 * {@code RESEND_FROM} sang địa chỉ thuộc domain đó.
 *
 * Thiết kế "không throw ra ngoài": lỗi gửi email (kể cả mất kết nối) không được làm hỏng luồng
 * nghiệp vụ gọi nó (vd forgot-password vẫn phải trả response generic anti-enumeration dù email
 * gửi thất bại) — caller chỉ nhận về true/false để log, không dùng để quyết định response.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestClient restClient;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${resend.api.from:TaskHub <onboarding@resend.dev>}")
    private String fromAddress;

    public EmailService() {
        // RestClient.create() — fresh client, giống pattern AiSuggestionService, không phụ
        // thuộc Spring-injected builder.
        this.restClient = RestClient.create();
    }

    public boolean isConfigured() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }

    /**
     * Gửi email HTML qua Resend. Trả false (không throw) nếu chưa cấu hình
     * RESEND_API_KEY hoặc gửi thất bại — xem javadoc class.
     */
    public boolean sendEmail(String to, String subject, String html) {
        if (!isConfigured()) {
            log.info("RESEND_API_KEY chưa cấu hình — bỏ qua gửi email tới {}", maskEmail(to));
            return false;
        }
        Map<String, Object> body = Map.of(
                "from", fromAddress,
                "to", List.of(to),
                "subject", subject,
                "html", html
        );
        try {
            restClient.post()
                    .uri(RESEND_URL)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Đã gửi email qua Resend tới {}", maskEmail(to));
            return true;
        } catch (RestClientResponseException e) {
            log.error("Resend trả lỗi {} khi gửi email tới {}: {}",
                    e.getStatusCode().value(), maskEmail(to), e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            log.error("Không gọi được Resend (lỗi kết nối) khi gửi email tới {}: {}",
                    maskEmail(to), e.getMessage());
            return false;
        }
    }

    /** Che bớt phần tên trong email khi log (không log PII đầy đủ). */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String maskedName = name.length() <= 2
                ? "**"
                : name.charAt(0) + "***" + name.charAt(name.length() - 1);
        return maskedName + email.substring(at);
    }
}
