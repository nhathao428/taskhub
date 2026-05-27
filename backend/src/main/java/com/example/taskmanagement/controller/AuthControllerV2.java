package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.AuthResponse;
import com.example.taskmanagement.dto.LoginRequest;
import com.example.taskmanagement.dto.RegisterRequestV2;
import com.example.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth API v2 — chỉ khác v1 ở chỗ password phải có chữ + số + ký tự đặc biệt.
 * Login giữ nguyên hành vi v1 (chỉ bcrypt-compare, không validate password format).
 */
@RestController
@RequestMapping("/api/v2/auth")
public class AuthControllerV2 {

    private final UserService userService;

    public AuthControllerV2(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequestV2 request) {
        AuthResponse response = userService.register(request.toV1());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
