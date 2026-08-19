package com.jmarcos.semumreal.port.out;

import java.util.Optional;

import com.jmarcos.semumreal.domain.model.GoogleIdentity;

public interface GoogleIdentityVerifierPort {
    Optional<GoogleIdentity> verify(String idToken);
}
