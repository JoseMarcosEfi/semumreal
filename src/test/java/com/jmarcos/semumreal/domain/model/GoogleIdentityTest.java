package com.jmarcos.semumreal.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GoogleIdentityTest {

    @Test
    void verifiedGmailIsGoogleAuthoritative() {
        GoogleIdentity identity = gmail("Jane");

        assertTrue(identity.isGoogleAuthoritative());
    }

    @Test
    void verifiedGooglemailIsGoogleAuthoritative() {
        GoogleIdentity identity = new GoogleIdentity(
                "sub-1", "jane@googlemail.com", "Jane", true, null);

        assertTrue(identity.isGoogleAuthoritative());
    }

    @Test
    void verifiedWorkspaceAccountWithHostedDomainIsGoogleAuthoritative() {
        GoogleIdentity identity = new GoogleIdentity(
                "sub-1", "jane@company.com", "Jane", true, "company.com");

        assertTrue(identity.isGoogleAuthoritative());
    }

    @Test
    void verifiedThirdPartyEmailWithoutHostedDomainIsNotAuthoritative() {
        GoogleIdentity identity = new GoogleIdentity(
                "sub-1", "jane@company.com", "Jane", true, null);

        assertFalse(identity.isGoogleAuthoritative());
    }

    @Test
    void unverifiedGmailIsNotAuthoritative() {
        GoogleIdentity identity = new GoogleIdentity(
                "sub-1", "jane@gmail.com", "Jane", false, null);

        assertFalse(identity.isGoogleAuthoritative());
    }

    @Test
    void normalizesEmail() {
        GoogleIdentity identity = new GoogleIdentity(
                "sub-1", "  Jane@Gmail.COM  ", "Jane", true, null);

        assertEquals("jane@gmail.com", identity.email());
    }

    @Test
    void displayNameFallsBackToEmailLocalPartWhenNameIsBlank() {
        GoogleIdentity identity = new GoogleIdentity(
                "sub-1", "jane@gmail.com", "  ", true, null);

        assertEquals("jane", identity.displayName());
    }

    @Test
    void rejectsBlankSubject() {
        assertThrows(IllegalArgumentException.class,
                () -> new GoogleIdentity(" ", "jane@gmail.com", "Jane", true, null));
    }

    private static GoogleIdentity gmail(String name) {
        return new GoogleIdentity("sub-1", "jane@gmail.com", name, true, null);
    }
}
