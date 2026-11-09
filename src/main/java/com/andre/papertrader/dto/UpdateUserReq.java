package com.andre.papertrader.dto;

import com.andre.papertrader.entity.UserRole;

public record UpdateUserReq(String username, String email, String phone, UserRole role) {
}
