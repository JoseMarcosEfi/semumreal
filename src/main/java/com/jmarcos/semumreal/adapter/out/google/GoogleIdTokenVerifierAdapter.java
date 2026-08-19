package com.jmarcos.semumreal.adapter.out.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jmarcos.semumreal.domain.model.GoogleIdentity;
import com.jmarcos.semumreal.port.out.GoogleIdentityVerifierPort;

@Component
public class GoogleIdTokenVerifierAdapter implements GoogleIdentityVerifierPort {
    private final GoogleIdTokenVerifier verifier;

    @Autowired
    public GoogleIdTokenVerifierAdapter(@Value("${app.google.client-ids:}") String clientIdsCsv) {
        this(buildVerifier(parseClientIds(clientIdsCsv)));
    }

    GoogleIdTokenVerifierAdapter(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Optional<GoogleIdentity> verify(String idToken) {
        if (verifier == null || idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }

        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                return Optional.empty();
            }
            return Optional.of(toIdentity(token.getPayload()));
        } catch (GeneralSecurityException | IOException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static GoogleIdentity toIdentity(Payload payload) {
        return new GoogleIdentity(
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("name"),
                Boolean.TRUE.equals(payload.getEmailVerified()),
                payload.getHostedDomain());
    }

    private static GoogleIdTokenVerifier buildVerifier(List<String> clientIds) {
        if (clientIds.isEmpty()) {
            return null;
        }
        try {
            return new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    new GsonFactory())
                    .setAudience(clientIds)
                    .build();
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Failed to initialize Google ID token verifier", ex);
        }
    }

    static List<String> parseClientIds(String clientIdsCsv) {
        if (clientIdsCsv == null || clientIdsCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(clientIdsCsv.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
    }
}
