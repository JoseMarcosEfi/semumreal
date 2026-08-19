package com.jmarcos.semumreal.adapter.out.persistence;

import com.jmarcos.semumreal.adapter.out.persistence.entity.UserEntity;
import com.jmarcos.semumreal.adapter.out.persistence.mapper.UserMapper;
import com.jmarcos.semumreal.adapter.out.persistence.repository.UserRepository;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements UserPersistencePort{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserPersistenceAdapter(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    @Override
    public User create(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
        .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
        .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByGoogleSubject(String googleSubject) {
        return userRepository.findByGoogleSubject(googleSubject)
                .map(userMapper::toDomain);
    }

    @Override
    public User update(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
