package com.andre.papertrader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        String name,
        String sameSite,
        boolean secure
) {
}
