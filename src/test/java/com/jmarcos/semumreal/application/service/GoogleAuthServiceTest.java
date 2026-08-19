package com.jmarcos.semumreal.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jmarcos.semumreal.domain.enums.Role;
import com.jmarcos.semumreal.domain.exception.EmailAlreadyExistsException;
import com.jmarcos.semumreal.domain.exception.InvalidGoogleTokenException;
import com.jmarcos.semumreal.domain.model.GoogleIdentity;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.GoogleIdentityVerifierPort;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    private static final String ID_TOKEN = "google-id-token";
    private static final String SUBJECT = "google-sub-123";
    private static final String GMAIL = "jane@gmail.com";

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private GoogleIdentityVerifierPort googleIdentityVerifierPort;

    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        googleAuthService = new GoogleAuthService(userPersistencePort, googleIdentityVerifierPort);
    }

    @Test
    void blankTokenThrowsInvalidGoogleToken() {
        assertThrows(InvalidGoogleTokenException.class, () -> googleAuthService.authenticate("  "));

        verify(googleIdentityVerifierPort, never()).verify(anyString());
    }

    @Test
    void invalidTokenThrowsInvalidGoogleToken() {
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.empty());

        assertThrows(InvalidGoogleTokenException.class, () -> googleAuthService.authenticate(ID_TOKEN));
    }

    @Test
    void unverifiedEmailThrowsInvalidGoogleToken() {
        when(googleIdentityVerifierPort.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleIdentity(SUBJECT, GMAIL, "Jane", false, null)));

        assertThrows(InvalidGoogleTokenException.class, () -> googleAuthService.authenticate(ID_TOKEN));

        verify(userPersistencePort, never()).findByGoogleSubject(anyString());
        verify(userPersistencePort, never()).create(any());
    }

    @Test
    void returningFederatedUserIsAuthenticatedBySubject() {
        User stored = User.reconstitute(1L, "Jane", GMAIL, null, Role.USER, SUBJECT);
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.of(gmailIdentity()));
        when(userPersistencePort.findByGoogleSubject(SUBJECT)).thenReturn(Optional.of(stored));

        User authenticated = googleAuthService.authenticate(ID_TOKEN);

        assertEquals(1L, authenticated.getId());
        verify(userPersistencePort, never()).create(any());
        verify(userPersistencePort, never()).update(any());
    }

    @Test
    void newGmailUserIsCreatedWithoutPassword() {
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.of(gmailIdentity()));
        when(userPersistencePort.findByGoogleSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail(GMAIL)).thenReturn(Optional.empty());
        when(userPersistencePort.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = googleAuthService.authenticate(ID_TOKEN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userPersistencePort).create(captor.capture());
        assertEquals(GMAIL, created.getEmail());
        assertEquals(SUBJECT, created.getGoogleSubject());
        assertNull(created.getPassword());
        assertEquals("Jane", captor.getValue().getName());
    }

    @Test
    void existingLocalGmailAccountIsLinkedWhenGoogleIsAuthoritative() {
        User localUser = User.reconstitute(7L, "Jane", GMAIL, "$2a$hash", Role.USER);
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.of(gmailIdentity()));
        when(userPersistencePort.findByGoogleSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail(GMAIL)).thenReturn(Optional.of(localUser));
        when(userPersistencePort.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User authenticated = googleAuthService.authenticate(ID_TOKEN);

        assertEquals(SUBJECT, authenticated.getGoogleSubject());
        verify(userPersistencePort).update(localUser);
        verify(userPersistencePort, never()).create(any());
    }

    @Test
    void existingLocalThirdPartyEmailIsNotLinkedWhenGoogleIsNotAuthoritative() {
        String email = "jane@company.com";
        User localUser = User.reconstitute(7L, "Jane", email, "$2a$hash", Role.USER);
        GoogleIdentity identity = new GoogleIdentity(SUBJECT, email, "Jane", true, null);
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.of(identity));
        when(userPersistencePort.findByGoogleSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail(email)).thenReturn(Optional.of(localUser));

        assertThrows(EmailAlreadyExistsException.class, () -> googleAuthService.authenticate(ID_TOKEN));

        verify(userPersistencePort, never()).update(any());
        verify(userPersistencePort, never()).create(any());
    }

    @Test
    void existingWorkspaceEmailIsLinkedWhenHostedDomainIsPresent() {
        String email = "jane@company.com";
        User localUser = User.reconstitute(7L, "Jane", email, "$2a$hash", Role.USER);
        GoogleIdentity identity = new GoogleIdentity(SUBJECT, email, "Jane", true, "company.com");
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.of(identity));
        when(userPersistencePort.findByGoogleSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail(email)).thenReturn(Optional.of(localUser));
        when(userPersistencePort.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User authenticated = googleAuthService.authenticate(ID_TOKEN);

        assertEquals(SUBJECT, authenticated.getGoogleSubject());
        verify(userPersistencePort).update(localUser);
    }

    @Test
    void newUserUsesEmailLocalPartWhenGoogleNameIsBlank() {
        GoogleIdentity identity = new GoogleIdentity(SUBJECT, GMAIL, "  ", true, null);
        when(googleIdentityVerifierPort.verify(ID_TOKEN)).thenReturn(Optional.of(identity));
        when(userPersistencePort.findByGoogleSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail(GMAIL)).thenReturn(Optional.empty());
        when(userPersistencePort.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        googleAuthService.authenticate(ID_TOKEN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userPersistencePort).create(captor.capture());
        assertEquals("jane", captor.getValue().getName());
    }

    private static GoogleIdentity gmailIdentity() {
        return new GoogleIdentity(SUBJECT, GMAIL, "Jane", true, null);
    }
}
