package com.andre.papertrader.dto;

import com.andre.papertrader.entity.User;

public record UserRes(Long id, String username, String email, String phone) {

    public static UserRes from(User user) {
        return new UserRes(user.getId(), user.getUsername(), user.getEmail(), user.getPhone());
    }
}
