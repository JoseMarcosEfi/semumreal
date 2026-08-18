package com.jmarcos.semumreal.adapter.in.web.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jmarcos.semumreal.adapter.in.dto.response.ErrorResponse;
import com.jmarcos.semumreal.adapter.in.web.config.PublicEndpoints;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.UserPersistencePort;
import com.jmarcos.semumreal.port.out.jwt.JwtPort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtPort jwtPort;
    private final UserPersistencePort userPersistencePort;
    private final JsonMapper jsonMapper;

    public JwtAuthenticationFilter(
            JwtPort jwtPort,
            UserPersistencePort userPersistencePort,
            JsonMapper jsonMapper) {
        this.jwtPort = jwtPort;
        this.userPersistencePort = userPersistencePort;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PublicEndpoints.matches(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!header.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, AuthErrorMessages.INVALID_TOKEN);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response, AuthErrorMessages.INVALID_TOKEN);
            return;
        }

        if (jwtPort.isTokenExpired(token)) {
            writeUnauthorized(response, AuthErrorMessages.EXPIRED_TOKEN);
            return;
        }

        if (!jwtPort.isTokenValid(token)) {
            writeUnauthorized(response, AuthErrorMessages.INVALID_TOKEN);
            return;
        }

        String email = jwtPort.extractEmail(token);
        User user = userPersistencePort.findByEmail(email).orElse(null);
        if (user == null) {
            writeUnauthorized(response, AuthErrorMessages.INVALID_TOKEN);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), new ErrorResponse(message));
    }
}
