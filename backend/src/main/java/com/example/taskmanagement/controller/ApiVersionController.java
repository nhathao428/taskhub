package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint public để client tự discover các phiên bản API đang chạy.
 * Dùng để mobile/web hiện màn "Có bản mới — vui lòng cập nhật" khi backend bỏ rơi
 * version cũ.
 */
@RestController
@RequestMapping("/api/version")
public class ApiVersionController {

    @GetMapping
    public ApiResponse<Map<String, Object>> version() {
        return ApiResponse.ok(Map.of(
                "current", "v2",
                "supported", List.of("v1", "v2"),
                // v1 = mặc định khi client gọi /api/foo (không có prefix version).
                // Sẽ bị deprecate trong phiên bản sau, client nên migrate sang v2.
                "defaultIfUnspecified", "v1",
                "deprecated", List.of("v1"),
                "endpoints", Map.of(
                        "v1Alias", "/api/v1/...",
                        "v2", "/api/v2/...",
                        "legacy", "/api/..."
                )
        ));
    }
}
