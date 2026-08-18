package com.jmarcos.semumreal.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jmarcos.semumreal.adapter.in.web.security.AuthErrorMessages;
import com.jmarcos.semumreal.adapter.out.jwt.JwtAdapter;
import com.jmarcos.semumreal.domain.enums.Role;
import com.jmarcos.semumreal.domain.model.User;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIT {

    private static final String RAW_PASSWORD = "password1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Test
    void publicEndpointIsAccessibleWithoutAuthentication() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("Jane", email, RAW_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginWithUnknownUserReturnsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("missing@example.com", RAW_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AuthErrorMessages.INVALID_CREDENTIALS));
    }

    @Test
    void loginWithWrongPasswordReturnsInvalidCredentials() throws Exception {
        String email = uniqueEmail();
        registerUser(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AuthErrorMessages.INVALID_CREDENTIALS));
    }

    @Test
    void loginWithValidCredentialsReturnsTokenWithoutPassword() throws Exception {
        String email = uniqueEmail();
        registerUser(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void protectedEndpointAllowsAccessWithValidJwt() throws Exception {
        String email = uniqueEmail();
        registerUser(email);
        String token = loginAndExtractToken(email);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void protectedEndpointRejectsRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AuthErrorMessages.MISSING_TOKEN));
    }

    @Test
    void protectedEndpointRejectsRequestWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer("not-a-valid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AuthErrorMessages.INVALID_TOKEN));
    }

    @Test
    void protectedEndpointRejectsExpiredJwt() throws Exception {
        String email = uniqueEmail();
        User user = User.reconstitute(1L, "Jane", email, "hash", Role.USER);
        String expiredToken = new JwtAdapter(jwtSecret, -1_000L).generateToken(user);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(expiredToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AuthErrorMessages.EXPIRED_TOKEN));
    }

    private void registerUser(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("Jane", email, RAW_PASSWORD)))
                .andExpect(status().isCreated());
    }

    private String loginAndExtractToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asString();
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private static String registerPayload(String name, String email, String password) {
        return """
                {"name":"%s","email":"%s","password":"%s"}
                """.formatted(name, email, password);
    }

    private static String loginPayload(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
