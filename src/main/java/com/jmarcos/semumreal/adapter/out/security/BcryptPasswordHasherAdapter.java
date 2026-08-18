package com.jmarcos.semumreal.adapter.out.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jmarcos.semumreal.port.out.PasswordHasherPort;

@Component
public class BcryptPasswordHasherAdapter implements PasswordHasherPort {
    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
