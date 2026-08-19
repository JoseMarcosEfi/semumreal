package com.jmarcos.semumreal.adapter.out.google;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.jmarcos.semumreal.domain.model.GoogleIdentity;

@ExtendWith(MockitoExtension.class)
class GoogleIdTokenVerifierAdapterTest {

    @Mock
    private GoogleIdTokenVerifier verifier;

    @Test
    void parseClientIdsSplitsAndTrimsCommaSeparatedValues() {
        List<String> clientIds = GoogleIdTokenVerifierAdapter.parseClientIds(
                " web-id.apps.googleusercontent.com , android-id.apps.googleusercontent.com ");

        assertEquals(
                List.of("web-id.apps.googleusercontent.com", "android-id.apps.googleusercontent.com"),
                clientIds);
    }

    @Test
    void blankTokenReturnsEmptyWithoutCallingGoogle() {
        GoogleIdTokenVerifierAdapter adapter = new GoogleIdTokenVerifierAdapter(verifier);

        assertTrue(adapter.verify("  ").isEmpty());
    }

    @Test
    void missingVerifierReturnsEmpty() {
        GoogleIdTokenVerifierAdapter adapter = new GoogleIdTokenVerifierAdapter((GoogleIdTokenVerifier) null);

        assertTrue(adapter.verify("token").isEmpty());
    }

    @Test
    void invalidTokenFromGoogleReturnsEmpty() throws GeneralSecurityException, IOException {
        when(verifier.verify("bad-token")).thenReturn(null);
        GoogleIdTokenVerifierAdapter adapter = new GoogleIdTokenVerifierAdapter(verifier);

        Optional<GoogleIdentity> identity = adapter.verify("bad-token");

        assertTrue(identity.isEmpty());
    }
}
