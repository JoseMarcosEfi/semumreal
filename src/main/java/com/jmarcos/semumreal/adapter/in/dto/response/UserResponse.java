package com.jmarcos.semumreal.adapter.in.dto.response;

import com.jmarcos.semumreal.domain.enums.Role;
import com.jmarcos.semumreal.domain.model.User;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole());
    }
}
