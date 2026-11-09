package com.andre.papertrader.service;

import com.andre.papertrader.config.JwtProperties;
import com.andre.papertrader.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken issue(User user) {
        String sessionId = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.tokenExpiration());

        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .id(sessionId)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new IssuedToken(token, sessionId, expiresAt);
    }

    public TokenClaims parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getSubject() == null || claims.getId() == null || claims.getId().isBlank()) {
            throw new JwtException("Token 缺少会话信息");
        }
        try {
            return new TokenClaims(Long.valueOf(claims.getSubject()), claims.getId());
        } catch (NumberFormatException e) {
            throw new JwtException("Token 用户标识无效", e);
        }
    }

    public record IssuedToken(String value, String sessionId, Instant expiresAt) {
    }

    public record TokenClaims(Long userId, String sessionId) {
    }
}
