package com.example.taskmanagement.config;

import com.example.taskmanagement.repository.AttendanceFaceCaptureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tự động xoá ảnh check-in nghi vấn đã quá hạn lưu trữ.
 *
 * VÌ SAO CẦN: ảnh khuôn mặt là dữ liệu sinh trắc học nhạy cảm. Giữ vô thời hạn vừa tăng
 * rủi ro nếu DB rò rỉ, vừa không đúng nguyên tắc "chỉ lưu trong thời gian cần thiết" của
 * Nghị định 13/2023. Hạn lưu cấu hình bằng app.face.capture-retention-days (mặc định 30
 * ngày) — đủ dài để quản lý kịp đối chiếu, đủ ngắn để không tích luỹ.
 *
 * Chạy mỗi ngày lúc 03:00 (giờ server) — thời điểm ít người dùng.
 */
@Component
public class FaceCaptureCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(FaceCaptureCleanupJob.class);

    private final AttendanceFaceCaptureRepository captureRepository;

    public FaceCaptureCleanupJob(AttendanceFaceCaptureRepository captureRepository) {
        this.captureRepository = captureRepository;
    }

    @Scheduled(cron = "${app.face.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void deleteExpiredCaptures() {
        LocalDateTime now = LocalDateTime.now();
        long pending = captureRepository.countByExpiresAtBefore(now);
        if (pending == 0) {
            return;
        }
        int deleted = captureRepository.deleteExpired(now);
        log.info("Dọn dẹp ảnh check-in nghi vấn: đã xoá {} ảnh quá hạn lưu trữ", deleted);
    }
}
