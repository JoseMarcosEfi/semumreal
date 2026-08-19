package com.jmarcos.semumreal.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "GoogleLoginRequest", description = "ID token emitido pelo Google Identity Services ou Google Sign-In")
public record GoogleLoginRequest(
        @NotBlank
        @Schema(
                description = "ID token JWT retornado pelo Google após o login no cliente (web, Android ou iOS)",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idToken) {
}
