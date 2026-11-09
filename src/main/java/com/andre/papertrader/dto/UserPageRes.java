package com.andre.papertrader.dto;

import java.util.List;

public record UserPageRes(
        List<UserRes> users,
        long total,
        int page,
        int size,
        int totalPages
) {
}
