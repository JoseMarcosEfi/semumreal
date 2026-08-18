package com.jmarcos.semumreal.port.out;

public interface PasswordHasherPort {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
