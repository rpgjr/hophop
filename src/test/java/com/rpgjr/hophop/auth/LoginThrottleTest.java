package com.rpgjr.hophop.auth;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginThrottleTest {

    private final MutableClock clock = new MutableClock();
    private final LoginThrottle throttle = new LoginThrottle(clock);

    @Test
    void blocksAClientAfterFiveWrongCodes() {
        for (int i = 0; i < 4; i++) throttle.failed("1.1.1.1");
        assertTrue(throttle.allowed("1.1.1.1"));
        throttle.failed("1.1.1.1");
        assertFalse(throttle.allowed("1.1.1.1"));
    }

    @Test
    void otherClientsAreNotAffectedByOne() {
        for (int i = 0; i < 5; i++) throttle.failed("1.1.1.1");
        assertTrue(throttle.allowed("2.2.2.2"));
    }

    @Test
    void unblocksAfterTheWindow() {
        for (int i = 0; i < 5; i++) throttle.failed("1.1.1.1");
        clock.advance(Duration.ofMinutes(6));
        assertTrue(throttle.allowed("1.1.1.1"));
    }

    @Test
    void aGoodCodeClearsTheClientsFailures() {
        for (int i = 0; i < 4; i++) throttle.failed("1.1.1.1");
        throttle.succeeded("1.1.1.1");
        for (int i = 0; i < 4; i++) throttle.failed("1.1.1.1");
        assertTrue(throttle.allowed("1.1.1.1"));
    }

    @Test
    void blocksEveryoneAfterTooManyFailuresOverall() {
        for (int i = 0; i < 30; i++) throttle.failed("ip-" + i);
        assertFalse(throttle.allowed("a-new-client"));
        clock.advance(Duration.ofMinutes(16));
        assertTrue(throttle.allowed("a-new-client"));
    }
}
