package com.andre.papertrader.controller;

import com.andre.papertrader.config.AuthCookieService;
import com.andre.papertrader.common.Result;
import com.andre.papertrader.dto.LoginReq;
import com.andre.papertrader.dto.LoginRes;
import com.andre.papertrader.service.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/login")
    public Result<LoginRes> login(@RequestBody LoginReq req, HttpServletResponse response) {
        AuthService.LoginResult loginResult = authService.login(req);
        authCookieService.writeToken(response, loginResult.accessToken());
        return Result.ok(loginResult.response());
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(authCookieService.getToken(request));
        authCookieService.clearToken(response);
        return Result.ok();
    }
}
