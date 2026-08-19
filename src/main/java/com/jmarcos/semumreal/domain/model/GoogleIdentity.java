package com.jmarcos.semumreal.domain.model;

public record GoogleIdentity(
        String subject,
        String email,
        String name,
        boolean emailVerified,
        String hostedDomain) {

    private static final String GMAIL_SUFFIX = "@gmail.com";
    private static final String GOOGLEMAIL_SUFFIX = "@googlemail.com";

    public GoogleIdentity {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Google subject is required");
        }
        email = User.normalizeEmail(email);
        name = name == null ? "" : name.trim();
        hostedDomain = isBlank(hostedDomain) ? null : hostedDomain.trim();
    }

    public boolean isGoogleAuthoritative() {
        return emailVerified && (isGmail() || hasHostedDomain());
    }

    public String displayName() {
        if (!isBlank(name)) {
            return name;
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private boolean isGmail() {
        return email.endsWith(GMAIL_SUFFIX) || email.endsWith(GOOGLEMAIL_SUFFIX);
    }

    private boolean hasHostedDomain() {
        return hostedDomain != null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
