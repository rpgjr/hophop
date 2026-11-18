package com.rpgjr.hophop.auth;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStoreTest {

    private final MutableClock clock = new MutableClock();
    private final SessionStore store = new SessionStore(clock, Duration.ofMinutes(30), Duration.ofHours(12));

    @Test
    void liveSessionIsAccepted() {
        assertTrue(store.touch(store.create()));
    }

    @Test
    void unknownOrMissingTokensAreRefused() {
        assertFalse(store.touch("nope"));
        assertFalse(store.touch(""));
        assertFalse(store.touch(null));
    }

    @Test
    void tokensAreUnique() {
        assertNotEquals(store.create(), store.create());
    }

    @Test
    void endsAfterIdleTime() {
        String token = store.create();
        clock.advance(Duration.ofMinutes(31));
        assertFalse(store.touch(token));
    }

    @Test
    void activityKeepsTheSessionOpen() {
        String token = store.create();
        for (int i = 0; i < 5; i++) {
            clock.advance(Duration.ofMinutes(20));
            assertTrue(store.touch(token), "still active after " + (i + 1) * 20 + " minutes");
        }
    }

    @Test
    void anExpiredSessionStaysExpired() {
        String token = store.create();
        clock.advance(Duration.ofMinutes(31));
        assertFalse(store.touch(token));
        clock.advance(Duration.ofSeconds(1));
        assertFalse(store.touch(token));
    }

    @Test
    void endsAtTheMaximumAgeEvenWhenActive() {
        String token = store.create();
        for (int i = 0; i < 25; i++) {
            clock.advance(Duration.ofMinutes(29));
            if (!store.touch(token)) return;
        }
        throw new AssertionError("session should have ended after 12 hours");
    }

    @Test
    void revokedSessionIsRefused() {
        String token = store.create();
        store.revoke(token);
        assertFalse(store.touch(token));
    }
}
