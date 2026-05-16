package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.UserDTO;
import com.example.taskmanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Quản lý tài khoản người dùng — chỉ ADMIN (xem SecurityConfig: /api/users/** = ADMIN).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(UserDTO::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    /** ADMIN đổi vai trò user — body: { "role": "MANAGER" | "EMPLOYEE" }. */
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserDTO>> updateRole(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        UserDTO updated = UserDTO.from(userService.updateUserRole(id, body.get("role")));
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}
