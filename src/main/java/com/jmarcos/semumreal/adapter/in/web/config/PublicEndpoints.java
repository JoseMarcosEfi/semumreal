package com.jmarcos.semumreal.adapter.in.web.config;

public final class PublicEndpoints {
    public static final String LOGIN = "/api/auth/login";
    public static final String REGISTER = "/api/auth/register";

    public static final String[] ALL = {
            LOGIN,
            REGISTER,
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    private PublicEndpoints() {
    }

    public static boolean matches(String path) {
        return LOGIN.equals(path)
                || REGISTER.equals(path)
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
