package com.andre.papertrader.dto;

import com.andre.papertrader.entity.User;
import com.andre.papertrader.entity.UserRole;

public record UserRes(Long id, String username, String email, String phone, UserRole role) {

    public static UserRes from(User user) {
        return new UserRes(user.getId(), user.getUsername(), user.getEmail(), user.getPhone(), user.getRole());
    }
}
