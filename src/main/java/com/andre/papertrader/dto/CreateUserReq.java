package com.andre.papertrader.dto;

public record CreateUserReq(String username, String password, String email, String phone) {
}
