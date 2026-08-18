package com.jmarcos.semumreal.adapter.out.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.jmarcos.semumreal.domain.enums.Role;
import com.jmarcos.semumreal.domain.model.User;

class JwtAdapterTest {

    private static final String SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
    private static final long ONE_HOUR_MS = 3_600_000L;

    @Test
    void generateTokenContainsEmailAndRole() {
        JwtAdapter jwtAdapter = adapter(ONE_HOUR_MS);
        User user = sampleUser();

        String token = jwtAdapter.generateToken(user);

        assertEquals(user.getEmail(), jwtAdapter.extractEmail(token));
        assertEquals(Role.USER.name(), jwtAdapter.extractRole(token));
        assertTrue(jwtAdapter.isTokenValid(token));
        assertFalse(jwtAdapter.isTokenExpired(token));
    }

    @Test
    void isTokenValidReturnsTrueForFreshToken() {
        JwtAdapter jwtAdapter = adapter(ONE_HOUR_MS);

        String token = jwtAdapter.generateToken(sampleUser());

        assertTrue(jwtAdapter.isTokenValid(token));
    }

    @Test
    void isTokenValidReturnsFalseForMalformedToken() {
        JwtAdapter jwtAdapter = adapter(ONE_HOUR_MS);

        assertFalse(jwtAdapter.isTokenValid("not-a-jwt"));
        assertFalse(jwtAdapter.isTokenExpired("not-a-jwt"));
    }

    @Test
    void isTokenValidReturnsFalseForTamperedToken() {
        JwtAdapter jwtAdapter = adapter(ONE_HOUR_MS);
        String token = jwtAdapter.generateToken(sampleUser());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtAdapter.isTokenValid(tampered));
    }

    @Test
    void isTokenExpiredReturnsTrueAndIsTokenValidReturnsFalseForExpiredToken() {
        Clock now = Clock.fixed(Instant.parse("2026-08-17T12:00:00Z"), ZoneOffset.UTC);
        JwtAdapter jwtAdapter = new JwtAdapter(SECRET, ONE_HOUR_MS, now);
        String token = jwtAdapter.generateToken(sampleUser());

        Clock afterExpiry = Clock.fixed(Instant.parse("2026-08-17T14:00:00Z"), ZoneOffset.UTC);
        JwtAdapter expiredReader = new JwtAdapter(SECRET, ONE_HOUR_MS, afterExpiry);

        assertTrue(expiredReader.isTokenExpired(token));
        assertFalse(expiredReader.isTokenValid(token));
    }

    private static JwtAdapter adapter(long expirationMs) {
        return new JwtAdapter(SECRET, expirationMs, Clock.systemUTC());
    }

    private static User sampleUser() {
        return User.reconstitute(1L, "Jane", "jane@example.com", "hash", Role.USER);
    }
}
