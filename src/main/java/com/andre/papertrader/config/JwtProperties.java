package com.andre.papertrader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth.jwt")
public record JwtProperties(
        String secret,
        Duration tokenExpiration,
        Duration sessionIdleTimeout
) {
}
