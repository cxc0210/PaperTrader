package com.andre.papertrader.dto;

import com.andre.papertrader.entity.UserRole;

public record CreateUserReq(String username, String password, String email, String phone, UserRole role) {
}
