package com.jmarcos.semumreal.adapter.in.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.jmarcos.semumreal.adapter.in.dto.request.GoogleLoginRequest;
import com.jmarcos.semumreal.adapter.in.dto.request.LoginRequest;
import com.jmarcos.semumreal.adapter.in.dto.request.RegisterUserRequest;
import com.jmarcos.semumreal.adapter.in.dto.response.LoginResponse;
import com.jmarcos.semumreal.adapter.in.dto.response.UserResponse;
import com.jmarcos.semumreal.adapter.in.web.config.OpenApiConfig;
import com.jmarcos.semumreal.application.service.AuthService;
import com.jmarcos.semumreal.application.service.GoogleAuthService;
import com.jmarcos.semumreal.application.service.UserService;
import com.jmarcos.semumreal.domain.exception.InvalidCredentialsException;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.jwt.JwtPort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final JwtPort jwtPort;

    public AuthController(
            UserService userService,
            AuthService authService,
            GoogleAuthService googleAuthService,
            JwtPort jwtPort) {
        this.userService = userService;
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.jwtPort = jwtPort;
    }

    @SecurityRequirements
    @Operation(summary = "Cadastrar usuário")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterUserRequest.class)))
            @RequestBody @Valid RegisterUserRequest request) {
        User user = userService.register(request.name(), request.email(), request.password());
        return UserResponse.from(user);
    }

    @SecurityRequirements
    @Operation(summary = "Autenticar usuário")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class)))
            @RequestBody @Valid LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String token = jwtPort.generateToken(user);
        return LoginResponse.from(token, user);
    }

    @SecurityRequirements
    @Operation(summary = "Autenticar usuário com Google")
    @PostMapping(value = "/google", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse loginWithGoogle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = GoogleLoginRequest.class)))
            @RequestBody @Valid GoogleLoginRequest request) {
        User user = googleAuthService.authenticate(request.idToken());
        String token = jwtPort.generateToken(user);
        return LoginResponse.from(token, user);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Consultar usuário autenticado",
            parameters = @Parameter(
                    name = HttpHeaders.AUTHORIZATION,
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Token JWT no formato: Bearer {token}",
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqYW5lQGV4YW1wbGUuY29tIn0.example"))
    @GetMapping("/me")
    public UserResponse me(@Parameter(hidden = true) Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new InvalidCredentialsException();
        }
        return UserResponse.from(user);
    }
}
