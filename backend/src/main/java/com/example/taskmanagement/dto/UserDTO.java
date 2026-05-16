package com.example.taskmanagement.dto;

import com.example.taskmanagement.entity.User;

/** Thông tin user trả ra cho client — KHÔNG kèm mật khẩu. */
public record UserDTO(Long userId, String username, String email, String role) {

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }
}
