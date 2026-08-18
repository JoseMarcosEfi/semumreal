package com.jmarcos.semumreal.adapter.in.web.config;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SemUmReal API",
                version = "0.0.1",
                description = "API de autenticação do SemUmReal"),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    GlobalOpenApiCustomizer bearerAuthCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents() == null
                    ? new Components()
                    : openApi.getComponents();
            openApi.setComponents(components);
            components.addSecuritySchemes(
                    BEARER_AUTH,
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                            .type(Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .in(In.HEADER)
                            .name("Authorization")
                            .description("Cole apenas o JWT retornado pelo login."));
            boolean alreadyDeclared = openApi.getSecurity() != null
                    && openApi.getSecurity().stream().anyMatch(requirement -> requirement.containsKey(BEARER_AUTH));
            if (!alreadyDeclared) {
                openApi.addSecurityItem(
                        new io.swagger.v3.oas.models.security.SecurityRequirement().addList(BEARER_AUTH));
            }
        };
    }
}
