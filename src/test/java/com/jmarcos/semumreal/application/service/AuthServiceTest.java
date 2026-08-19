package com.jmarcos.semumreal.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jmarcos.semumreal.domain.enums.Role;
import com.jmarcos.semumreal.domain.exception.InvalidCredentialsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.PasswordHasherPort;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "jane@example.com";
    private static final String RAW_PASSWORD = "password1";
    private static final String PASSWORD_HASH = "$2a$hashed";

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userPersistencePort, passwordHasherPort);
    }

    @Test
    void authenticateWithValidCredentialsReturnsUser() {
        User storedUser = storedUser();
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(storedUser));
        when(passwordHasherPort.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

        User authenticated = authService.authenticate(EMAIL, RAW_PASSWORD);

        assertEquals(EMAIL, authenticated.getEmail());
        assertEquals("Jane", authenticated.getName());
        assertEquals(Role.USER, authenticated.getRole());
    }

    @Test
    void authenticateNormalizesEmailBeforeLookup() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(storedUser()));
        when(passwordHasherPort.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

        User authenticated = authService.authenticate("  jane@example.com  ", RAW_PASSWORD);

        assertEquals(EMAIL, authenticated.getEmail());
        verify(userPersistencePort).findByEmail(EMAIL);
    }

    @Test
    void authenticateWithUnknownUserThrowsInvalidCredentials() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate(EMAIL, RAW_PASSWORD));

        verify(passwordHasherPort, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticateWithWrongPasswordThrowsInvalidCredentials() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(storedUser()));
        when(passwordHasherPort.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate(EMAIL, RAW_PASSWORD));
    }

    @Test
    void authenticateGoogleOnlyUserWithPasswordThrowsInvalidCredentials() {
        User googleUser = User.reconstitute(1L, "Jane", EMAIL, null, Role.USER, "google-sub");
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(googleUser));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate(EMAIL, RAW_PASSWORD));

        verify(passwordHasherPort, never()).matches(anyString(), anyString());
    }

    private static User storedUser() {
        return User.reconstitute(1L, "Jane", EMAIL, PASSWORD_HASH, Role.USER);
    }
}
