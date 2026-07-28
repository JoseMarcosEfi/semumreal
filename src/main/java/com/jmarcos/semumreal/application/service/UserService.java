package com.jmarcos.semumreal.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jmarcos.semumreal.domain.exception.EmailAlreadyExistsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@Service
public class UserService {
    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserPersistencePort userPersistencePort, PasswordEncoder passwordEncoder) {
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String name, String email, String password) {
        if(userPersistencePort.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.create(name, email, password);
        String rawPassword = passwordEncoder.encode(user.getPassword());
        user.changePasswordHash(rawPassword);

        return userPersistencePort.create(user);
    }

}
