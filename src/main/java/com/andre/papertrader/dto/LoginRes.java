package com.andre.papertrader.dto;

import java.time.Instant;

public record LoginRes(
        Instant tokenExpiresAt,
        Instant idleExpiresAt,
        UserRes user
) {
}
