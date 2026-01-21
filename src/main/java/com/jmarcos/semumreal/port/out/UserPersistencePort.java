package com.jmarcos.semumreal.port.out;

import java.util.Optional;

import com.jmarcos.semumreal.domain.model.User;

public interface UserPersistencePort {
    User create(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    void deleteById(Long id);
}
