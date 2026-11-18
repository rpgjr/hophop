package com.rpgjr.hophop.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Signed-in sessions, kept in memory. A session ends after an idle period with no requests,
 * or after a hard maximum age. A restart signs everyone out, which suits a single owner.
 */
@Component
public class SessionStore {

    private record Session(Instant created, Instant lastSeen) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration idle;
    private final Duration max;

    @Autowired
    public SessionStore(@Value("${auth.idle-seconds:1800}") long idleSeconds,
                        @Value("${auth.max-hours:12}") long maxHours) {
        this(Clock.systemUTC(), Duration.ofSeconds(idleSeconds), Duration.ofHours(maxHours));
    }

    SessionStore(Clock clock, Duration idle, Duration max) {
        this.clock = clock;
        this.idle = idle;
        this.max = max;
    }

    public Duration idleTimeout() {
        return idle;
    }

    /** Starts a session and returns its token. Only a hash of the token is kept. */
    public String create() {
        purgeExpired();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = clock.instant();
        sessions.put(hash(token), new Session(now, now));
        return token;
    }

    /** True when the token is a live session. A live session counts as activity and stays open longer. */
    public boolean touch(String token) {
        if (token == null || token.isBlank()) return false;
        String key = hash(token);
        Instant now = clock.instant();
        boolean[] live = {false};
        sessions.computeIfPresent(key, (k, session) -> {
            boolean expired = Duration.between(session.lastSeen(), now).compareTo(idle) > 0
                    || Duration.between(session.created(), now).compareTo(max) > 0;
            if (expired) return null;
            live[0] = true;
            return new Session(session.created(), now);
        });
        return live[0];
    }

    public void revoke(String token) {
        if (token != null) sessions.remove(hash(token));
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        sessions.entrySet().removeIf(e ->
                Duration.between(e.getValue().lastSeen(), now).compareTo(idle) > 0
                        || Duration.between(e.getValue().created(), now).compareTo(max) > 0);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
