package com.andre.papertrader.config;

import com.andre.papertrader.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            AuthService authService,
            AuthCookieService authCookieService,
            RestAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = authCookieService.getToken(request);
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthService.AuthenticatedUser authenticatedUser = authService.authenticate(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser.userId(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.role().name()))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (ResponseStatusException e) {
            SecurityContextHolder.clearContext();
            authCookieService.clearToken(response);
            deny(request, response);
        }
    }

    private void deny(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        );
    }
}
