package com.jmarcos.semumreal.adapter.in.dto.response;

import com.jmarcos.semumreal.domain.model.User;

public record LoginResponse(
        String token,
        UserResponse user) {

    public static LoginResponse from(String token, User user) {
        return new LoginResponse(token, UserResponse.from(user));
    }
}
