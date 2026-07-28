package com.jmarcos.semumreal.adapter.in.dto.request;

public record RegisterUserRequest(
        String name,
        String email,
        String password) {
}
