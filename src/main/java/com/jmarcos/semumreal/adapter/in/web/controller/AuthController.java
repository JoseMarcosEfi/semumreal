package com.jmarcos.semumreal.adapter.in.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jmarcos.semumreal.adapter.in.dto.request.LoginRequest;
import com.jmarcos.semumreal.adapter.in.dto.request.RegisterUserRequest;
import com.jmarcos.semumreal.adapter.in.dto.response.LoginResponse;
import com.jmarcos.semumreal.adapter.in.dto.response.UserResponse;
import com.jmarcos.semumreal.application.service.AuthService;
import com.jmarcos.semumreal.application.service.UserService;
import com.jmarcos.semumreal.domain.exception.InvalidCredentialsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.jwt.JwtPort;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final JwtPort jwtPort;

    public AuthController(UserService userService, AuthService authService, JwtPort jwtPort) {
        this.userService = userService;
        this.authService = authService;
        this.jwtPort = jwtPort;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterUserRequest request) {
        User user = userService.register(request.name(), request.email(), request.password());
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String token = jwtPort.generateToken(user);
        return LoginResponse.from(token, user);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new InvalidCredentialsException();
        }
        return UserResponse.from(user);
    }
}
