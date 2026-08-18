package com.jmarcos.semumreal.port.out.jwt;

import com.jmarcos.semumreal.domain.model.User;

public interface JwtPort {

    String generateToken(User user);

    String extractEmail(String token);

    String extractRole(String token);

    boolean isTokenValid(String token);

    boolean isTokenExpired(String token);
}