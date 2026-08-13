package com.example.taskmanagement.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * Cache in-memory dùng làm fallback khi KHÔNG có Redis (spring.cache.type=none hoặc
 * không set — đây chính là cấu hình mặc định trên free tier, xem render.yaml đặt
 * CACHE_TYPE=none để né chi phí Redis).
 *
 * Vấn đề trước khi có class này: {@link RedisConfig} là nơi DUY NHẤT khai báo
 * {@code @EnableCaching}, và nó chỉ active khi spring.cache.type=redis. Khi không có
 * Redis, không có CacheManager nào được đăng ký → toàn bộ {@code @Cacheable} trên
 * {@code AiSuggestionService} (gọi Gemini) bị vô hiệu hoàn toàn: mỗi request, kể cả bấm
 * lại y hệt 1 task trong vài giây, đều tốn 1 lần gọi AI thật — rất phí quota free tier
 * khi số lượng manager dùng tính năng gợi ý AI tăng lên.
 *
 * Cache ở đây là in-memory, single-instance (mất khi restart, không share giữa nhiều
 * instance như Redis) — chấp nhận được vì free tier (Render...) chỉ chạy 1 instance.
 * TTL giữ đúng thiết kế ban đầu ở RedisConfig: ai_suggestions 5 phút, user_details 60s.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "none", matchIfMissing = true)
public class InMemoryCacheConfig {

    private static final Map<String, Duration> CACHE_TTLS = Map.of(
            "ai_suggestions", Duration.ofMinutes(5),
            "user_details", Duration.ofSeconds(60)
    );
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    @Bean
    public CacheManager cacheManager() {
        return new ExpiringInMemoryCacheManager(CACHE_TTLS, DEFAULT_TTL);
    }
}
