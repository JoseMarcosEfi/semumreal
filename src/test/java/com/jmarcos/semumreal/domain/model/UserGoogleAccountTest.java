package com.jmarcos.semumreal.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.jmarcos.semumreal.domain.enums.Role;

class UserGoogleAccountTest {

    @Test
    void createFromGoogleCreatesUserWithoutPassword() {
        User user = User.createFromGoogle("Jane", "jane@gmail.com", "google-sub");

        assertEquals("Jane", user.getName());
        assertEquals("jane@gmail.com", user.getEmail());
        assertEquals("google-sub", user.getGoogleSubject());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getPassword());
    }

    @Test
    void createFromGoogleRejectsBlankSubject() {
        assertThrows(IllegalArgumentException.class,
                () -> User.createFromGoogle("Jane", "jane@gmail.com", " "));
    }

    @Test
    void linkGoogleSubjectAttachesGoogleAccountToLocalUser() {
        User user = User.create("Jane", "jane@gmail.com", "password1");

        user.linkGoogleSubject("google-sub");

        assertEquals("google-sub", user.getGoogleSubject());
    }

    @Test
    void linkGoogleSubjectIsIdempotentForSameSubject() {
        User user = User.createFromGoogle("Jane", "jane@gmail.com", "google-sub");

        user.linkGoogleSubject("google-sub");

        assertEquals("google-sub", user.getGoogleSubject());
    }

    @Test
    void linkGoogleSubjectRejectsDifferentSubject() {
        User user = User.createFromGoogle("Jane", "jane@gmail.com", "google-sub");

        assertThrows(IllegalStateException.class, () -> user.linkGoogleSubject("other-sub"));
    }
}
