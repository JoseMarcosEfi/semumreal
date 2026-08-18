package com.jmarcos.semumreal.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequest", description = "Credenciais de autenticação")
public record LoginRequest(
        @NotBlank
        @Email
        @Schema(description = "E-mail do usuário", example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @NotBlank
        @Size(min = 8)
        @Schema(description = "Senha do usuário", example = "password1", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String password) {
}
