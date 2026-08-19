package com.jmarcos.semumreal.adapter.out.persistence.mapper;

import org.springframework.stereotype.Component;

import com.jmarcos.semumreal.adapter.out.persistence.entity.UserEntity;
import com.jmarcos.semumreal.domain.model.User;

@Component
public class UserMapper {
    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return User.reconstitute(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getRole(),
                userEntity.getGoogleSubject());
    }
    public UserEntity toEntity(User user){
        if(user == null){
            return null;
        }
        return new UserEntity(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.getGoogleSubject()
        );
    }
}
