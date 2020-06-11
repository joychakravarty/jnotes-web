package com.jc.jnotesweb.security;

import javax.annotation.concurrent.Immutable;

import lombok.Getter;

@Getter
@Immutable
public class AuthResponse {

    private final AuthStatus authStatus;
    private final String userId;
    private final String encryptionKey;

    public static final AuthResponse INVALID_HEADER_AUTH_RESPONSE = new AuthResponse(AuthStatus.INVALID_HEADER, null, null);
    public static final AuthResponse INVALID_USER_AUTH_RESPONSE = new AuthResponse(AuthStatus.INVALID_USER, null, null);
    public static final AuthResponse INVALID_SECRET_AUTH_RESPONSE = new AuthResponse(AuthStatus.INVALID_SECRET, null, null);

    private AuthResponse(AuthStatus authStatus, String userId, String encryptionKey) {
        this.authStatus = authStatus;
        this.userId = userId;
        this.encryptionKey = encryptionKey;
    }

    public AuthResponse(String userId, String encryptionKey) {
        this.authStatus = AuthStatus.VALID;
        this.userId = userId;
        this.encryptionKey = encryptionKey;
    }

    public boolean isAuthenticationSuccessful() {
        if (authStatus == AuthStatus.VALID) {
            return true;
        } else {
            return false;
        }
    }
}
