package com.jmarcos.semumreal.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterUserRequest", description = "Dados para cadastro de usuário")
public record RegisterUserRequest(
        @NotBlank
        @Schema(description = "Nome do usuário", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @NotBlank
        @Email
        @Schema(description = "E-mail do usuário", example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @NotBlank
        @Size(min = 8)
        @Schema(description = "Senha do usuário", example = "password1", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String password) {
}
