package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.OfficeLocationRequest;
import com.example.taskmanagement.entity.OfficeLocation;
import com.example.taskmanagement.service.OfficeLocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/office-locations")
public class OfficeLocationController {

    private final OfficeLocationService service;

    public OfficeLocationController(OfficeLocationService service) {
        this.service = service;
    }

    /** Mọi user đã đăng nhập đều xem được danh sách offices (cần để hiển thị bản đồ check-in). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OfficeLocation>>> list(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.ok(activeOnly ? service.listActive() : service.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfficeLocation>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(id)));
    }

    /** Tạo / sửa / xóa office: chỉ MANAGER hoặc ADMIN (chặn ở SecurityConfig). */
    @PostMapping
    public ResponseEntity<ApiResponse<OfficeLocation>> create(@Valid @RequestBody OfficeLocationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OfficeLocation>> update(
            @PathVariable Long id, @Valid @RequestBody OfficeLocationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
