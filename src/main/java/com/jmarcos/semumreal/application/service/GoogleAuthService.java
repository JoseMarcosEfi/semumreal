package com.jmarcos.semumreal.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmarcos.semumreal.domain.exception.EmailAlreadyExistsException;
import com.jmarcos.semumreal.domain.exception.InvalidGoogleTokenException;
import com.jmarcos.semumreal.domain.model.GoogleIdentity;
import com.jmarcos.semumreal.domain.model.User;
import com.jmarcos.semumreal.port.out.GoogleIdentityVerifierPort;
import com.jmarcos.semumreal.port.out.UserPersistencePort;

@Service
public class GoogleAuthService {
    private final UserPersistencePort userPersistencePort;
    private final GoogleIdentityVerifierPort googleIdentityVerifierPort;

    public GoogleAuthService(
            UserPersistencePort userPersistencePort,
            GoogleIdentityVerifierPort googleIdentityVerifierPort) {
        this.userPersistencePort = userPersistencePort;
        this.googleIdentityVerifierPort = googleIdentityVerifierPort;
    }

    @Transactional
    public User authenticate(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidGoogleTokenException();
        }

        GoogleIdentity identity = googleIdentityVerifierPort.verify(idToken)
                .orElseThrow(InvalidGoogleTokenException::new);

        if (!identity.emailVerified()) {
            throw new InvalidGoogleTokenException();
        }

        return userPersistencePort.findByGoogleSubject(identity.subject())
                .orElseGet(() -> resolveByEmailOrCreate(identity));
    }

    private User resolveByEmailOrCreate(GoogleIdentity identity) {
        return userPersistencePort.findByEmail(identity.email())
                .map(existing -> linkGoogleAccount(existing, identity))
                .orElseGet(() -> createGoogleUser(identity));
    }

    private User linkGoogleAccount(User existing, GoogleIdentity identity) {
        if (existing.getGoogleSubject() == null && !identity.isGoogleAuthoritative()) {
            throw new EmailAlreadyExistsException(identity.email());
        }
        existing.linkGoogleSubject(identity.subject());
        return userPersistencePort.update(existing);
    }

    private User createGoogleUser(GoogleIdentity identity) {
        User user = User.createFromGoogle(
                identity.displayName(),
                identity.email(),
                identity.subject());
        return userPersistencePort.create(user);
    }
}
