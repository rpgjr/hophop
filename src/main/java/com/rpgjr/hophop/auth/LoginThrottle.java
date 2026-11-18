package com.rpgjr.hophop.auth;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down guessing. Too many wrong codes from one address, or too many overall,
 * and sign-in is refused until the older failures age out.
 */
@Component
public class LoginThrottle {

    private static final int PER_CLIENT_LIMIT = 5;
    private static final Duration PER_CLIENT_WINDOW = Duration.ofMinutes(5);
    private static final int GLOBAL_LIMIT = 30;
    private static final Duration GLOBAL_WINDOW = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, Deque<Instant>> byClient = new ConcurrentHashMap<>();
    private final Deque<Instant> overall = new ArrayDeque<>();

    public LoginThrottle() {
        this(Clock.systemUTC());
    }

    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    public boolean allowed(String client) {
        Instant now = clock.instant();
        synchronized (overall) {
            trim(overall, now, GLOBAL_WINDOW);
            if (overall.size() >= GLOBAL_LIMIT) return false;
        }
        Deque<Instant> failures = byClient.get(client);
        if (failures == null) return true;
        synchronized (failures) {
            trim(failures, now, PER_CLIENT_WINDOW);
            return failures.size() < PER_CLIENT_LIMIT;
        }
    }

    public void failed(String client) {
        Instant now = clock.instant();
        synchronized (overall) {
            overall.addLast(now);
        }
        Deque<Instant> failures = byClient.computeIfAbsent(client, k -> new ArrayDeque<>());
        synchronized (failures) {
            failures.addLast(now);
        }
    }

    public void succeeded(String client) {
        byClient.remove(client);
    }

    private static void trim(Deque<Instant> times, Instant now, Duration window) {
        while (!times.isEmpty() && Duration.between(times.peekFirst(), now).compareTo(window) > 0) {
            times.removeFirst();
        }
    }
}
