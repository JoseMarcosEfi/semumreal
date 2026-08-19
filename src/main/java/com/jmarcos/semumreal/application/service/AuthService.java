package com.jmarcos.semumreal.application.service;

import org.springframework.stereotype.Service;

import com.jmarcos.semumreal.domain.exception.InvalidCredentialsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.PasswordHasherPort;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@Service
public class AuthService {
    private final UserPersistencePort userPersistencePort;
    private final PasswordHasherPort passwordHasherPort;

    public AuthService(UserPersistencePort userPersistencePort, PasswordHasherPort passwordHasherPort) {
        this.userPersistencePort = userPersistencePort;
        this.passwordHasherPort = passwordHasherPort;
    }

    public User authenticate(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            throw new InvalidCredentialsException();
        }

        String normalizedEmail = User.normalizeEmail(email);
        User user = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPassword() == null || !passwordHasherPort.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
