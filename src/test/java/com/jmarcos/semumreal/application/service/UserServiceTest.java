package com.jmarcos.semumreal.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jmarcos.semumreal.domain.enums.Role;
import com.jmarcos.semumreal.domain.exception.EmailAlreadyExistsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.PasswordHasherPort;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "jane@example.com";

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userPersistencePort, passwordHasherPort);
    }

    @Test
    void registerNormalizesEmailBeforeUniquenessCheck() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash("password1")).thenReturn("$2a$hashed");
        when(userPersistencePort.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register("Jane", "  Jane@Example.com  ", "password1");

        verify(userPersistencePort).findByEmail(EMAIL);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userPersistencePort).create(userCaptor.capture());
        assertEquals(EMAIL, userCaptor.getValue().getEmail());
    }

    @Test
    void registerWithExistingEmailDifferentCaseThrows() {
        when(userPersistencePort.findByEmail(EMAIL))
                .thenReturn(Optional.of(User.reconstitute(1L, "Jane", EMAIL, "hash", Role.USER)));

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.register("Jane", "JANE@EXAMPLE.COM", "password1"));
    }
}
