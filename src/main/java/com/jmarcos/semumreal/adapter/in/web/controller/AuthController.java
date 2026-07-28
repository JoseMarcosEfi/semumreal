package com.jmarcos.semumreal.adapter.in.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.jmarcos.semumreal.adapter.in.dto.request.RegisterUserRequest;
import com.jmarcos.semumreal.adapter.in.dto.response.UserResponse;
import com.jmarcos.semumreal.application.service.UserService;
import com.jmarcos.semumreal.domain.model.User;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterUserRequest request){
        User user = userService.register(request.name(), request.email(), request.password());
        return UserResponse.from(user);
    }
}
