package com.example.taskmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableScheduling: bật cho FaceCaptureCleanupJob — tự xoá ảnh check-in nghi vấn quá hạn
 * lưu trữ (dữ liệu sinh trắc học không giữ vô thời hạn).
 */
@SpringBootApplication
@EnableScheduling
public class TaskManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }
}
