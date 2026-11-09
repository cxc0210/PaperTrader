package com.andre.papertrader.service;

import com.andre.papertrader.config.JwtProperties;
import com.andre.papertrader.dto.LoginReq;
import com.andre.papertrader.dto.LoginRes;
import com.andre.papertrader.dto.UserRes;
import com.andre.papertrader.entity.User;
import com.andre.papertrader.entity.UserRole;
import com.andre.papertrader.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AuthService {

    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String USER_SESSION_KEY_PREFIX = "auth:user-session:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RedisService redisService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisService = redisService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResult login(LoginReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        JwtService.IssuedToken issuedToken = jwtService.issue(user);
        Instant idleExpiresAt = Instant.now().plus(jwtProperties.sessionIdleTimeout());
        redisService.replaceSession(
                userSessionKey(user.getId()),
                sessionKey(issuedToken.sessionId()),
                String.valueOf(user.getId()),
                jwtProperties.sessionIdleTimeout()
        );

        return new LoginResult(
                new LoginRes(
                        issuedToken.expiresAt(),
                        idleExpiresAt,
                        UserRes.from(user)
                ),
                issuedToken.value()
        );
    }

    public record LoginResult(LoginRes response, String accessToken) {
    }

    public void logout(String token) {
        try {
            JwtService.TokenClaims claims = jwtService.parse(token);
            redisService.deleteSession(
                    sessionKey(claims.sessionId()),
                    userSessionKey(claims.userId())
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw invalidToken();
        }
    }

    public AuthenticatedUser authenticate(String token) {
        try {
            JwtService.TokenClaims claims = jwtService.parse(token);
            boolean active = redisService.refreshSession(
                    sessionKey(claims.sessionId()),
                    userSessionKey(claims.userId()),
                    String.valueOf(claims.userId()),
                    jwtProperties.sessionIdleTimeout()
            );
            if (!active) {
                throw invalidToken();
            }
            User user = userRepository.findById(claims.userId())
                    .orElseThrow(this::invalidToken);
            return new AuthenticatedUser(user.getId(), user.getRole());
        } catch (JwtException | IllegalArgumentException e) {
            throw invalidToken();
        }
    }

    public record AuthenticatedUser(Long userId, UserRole role) {
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String userSessionKey(Long userId) {
        return USER_SESSION_KEY_PREFIX + userId;
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误");
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token 无效或已过期");
    }
}
