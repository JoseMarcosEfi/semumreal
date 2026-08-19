package com.jmarcos.semumreal.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jmarcos.semumreal.adapter.in.web.security.AuthErrorMessages;
import com.jmarcos.semumreal.domain.model.GoogleIdentity;
import com.jmarcos.semumreal.port.out.GoogleIdentityVerifierPort;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAuthenticationTest {

    private static final String ID_TOKEN = "google-id-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private GoogleIdentityVerifierPort googleIdentityVerifierPort;

    @Test
    void googleLoginRejectsBlankToken() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void googleLoginRejectsInvalidToken() throws Exception {
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googlePayload(ID_TOKEN)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AuthErrorMessages.INVALID_GOOGLE_TOKEN));
    }

    @Test
    void googleLoginCreatesUserAndReturnsJwt() throws Exception {
        String email = uniqueGmail();
        String subject = "google-sub-" + UUID.randomUUID();
        when(googleIdentityVerifierPort.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleIdentity(subject, email, "Jane", true, null)));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googlePayload(ID_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.name").value("Jane"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void googleLoginJwtGrantsAccessToMe() throws Exception {
        String email = uniqueGmail();
        String subject = "google-sub-" + UUID.randomUUID();
        when(googleIdentityVerifierPort.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleIdentity(subject, email, "Jane", true, null)));

        MvcResult result = mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googlePayload(ID_TOKEN)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
        String token = body.get("token").asString();

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void googleLoginLinksExistingLocalGmailAccount() throws Exception {
        String email = uniqueGmail();
        registerUser(email);
        String subject = "google-sub-" + UUID.randomUUID();
        when(googleIdentityVerifierPort.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleIdentity(subject, email, "Jane", true, null)));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googlePayload(ID_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void googleLoginDoesNotTakeOverExistingThirdPartyEmail() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@company.com";
        registerUser(email);
        String subject = "google-sub-" + UUID.randomUUID();
        when(googleIdentityVerifierPort.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleIdentity(subject, email, "Jane", true, null)));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googlePayload(ID_TOKEN)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: " + email));
    }

    private void registerUser(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jane","email":"%s","password":"password1"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    private static String uniqueGmail() {
        return "user-" + UUID.randomUUID() + "@gmail.com";
    }

    private static String googlePayload(String idToken) {
        return """
                {"idToken":"%s"}
                """.formatted(idToken);
    }
}
