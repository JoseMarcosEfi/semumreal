package com.jmarcos.semumreal.adapter.in.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterUserRequest", description = "Dados para cadastro de usuário")
public record RegisterUserRequest(
        @Schema(description = "Nome do usuário", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "E-mail do usuário", example = "jane@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Senha do usuário", example = "password1", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String password) {
}
