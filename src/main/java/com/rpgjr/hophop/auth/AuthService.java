package com.rpgjr.hophop.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;

/**
 * Checks the authenticator code and starts sessions. There is one owner and no password:
 * the shared secret in AUTH_TOTP_SECRET is the only way in.
 */
@Service
public class AuthService {

    private final String secretText;
    private final SessionStore sessions;
    private final LoginThrottle throttle;
    private final Clock clock = Clock.systemUTC();

    private byte[] secret;
    private long lastUsedStep = -1;

    public AuthService(@Value("${auth.totp-secret:}") String secretText,
                       SessionStore sessions,
                       LoginThrottle throttle) {
        this.secretText = secretText == null ? "" : secretText.trim();
        this.sessions = sessions;
        this.throttle = throttle;
    }

    @PostConstruct
    void readSecret() {
        if (secretText.isEmpty()) return;
        try {
            secret = Base32.decode(secretText);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("AUTH_TOTP_SECRET must be a base32 string (A-Z and 2-7)", e);
        }
        if (secret.length < 10) {
            throw new IllegalStateException("AUTH_TOTP_SECRET is too short. Use at least 16 base32 characters");
        }
    }

    /** False means nobody can sign in, so the API stays locked. */
    public boolean isConfigured() {
        return secret != null;
    }

    byte[] secretBytes() {
        return secret;
    }

    public String secretBase32() {
        return Base32.encode(secret);
    }

    public Authorization login(String code, String client) {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Sign-in is not set up on the server");
        }
        if (!throttle.allowed(client)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Wait a few minutes");
        }

        String clean = code == null ? "" : code.replace(" ", "").trim();
        synchronized (this) {
            long step = Totp.match(secret, clean, Totp.stepAt(clock.instant()), 1);
            // a code works once: a step at or before the last accepted one is a replay
            if (step < 0 || step <= lastUsedStep) {
                throttle.failed(client);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "That code is not valid");
            }
            lastUsedStep = step;
        }
        throttle.succeeded(client);
        return new Authorization(sessions.create(), sessions.idleTimeout().toSeconds());
    }

    public record Authorization(String token, long idleSeconds) {
    }
}
