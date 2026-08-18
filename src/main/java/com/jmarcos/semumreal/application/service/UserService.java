package com.jmarcos.semumreal.application.service;

import org.springframework.stereotype.Service;

import com.jmarcos.semumreal.domain.exception.EmailAlreadyExistsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.PasswordHasherPort;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@Service
public class UserService {
    private final UserPersistencePort userPersistencePort;
    private final PasswordHasherPort passwordHasherPort;

    public UserService(UserPersistencePort userPersistencePort, PasswordHasherPort passwordHasherPort) {
        this.userPersistencePort = userPersistencePort;
        this.passwordHasherPort = passwordHasherPort;
    }

    public User register(String name, String email, String password) {
        if(userPersistencePort.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.create(name, email, password);
        user.changePasswordHash(passwordHasherPort.hash(user.getPassword()));

        return userPersistencePort.create(user);
    }

}
