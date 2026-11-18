package com.rpgjr.hophop.auth;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TotpTest {

    // RFC 6238 appendix B, SHA-1, secret "12345678901234567890". The RFC lists 8 digits; the app uses the last 6.
    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    @Test
    void matchesRfc6238Vectors() {
        assertEquals("287082", Totp.code(RFC_SECRET, Totp.stepAt(Instant.ofEpochSecond(59))));
        assertEquals("081804", Totp.code(RFC_SECRET, Totp.stepAt(Instant.ofEpochSecond(1111111109L))));
        assertEquals("005924", Totp.code(RFC_SECRET, Totp.stepAt(Instant.ofEpochSecond(1234567890L))));
    }

    @Test
    void acceptsOneStepOfClockDriftButNotMore() {
        long now = Totp.stepAt(Instant.ofEpochSecond(1111111109L));
        assertEquals(now, Totp.match(RFC_SECRET, Totp.code(RFC_SECRET, now), now, 1));
        assertEquals(now - 1, Totp.match(RFC_SECRET, Totp.code(RFC_SECRET, now - 1), now, 1));
        assertEquals(now + 1, Totp.match(RFC_SECRET, Totp.code(RFC_SECRET, now + 1), now, 1));
        assertEquals(-1, Totp.match(RFC_SECRET, Totp.code(RFC_SECRET, now - 3), now, 1));
    }

    @Test
    void rejectsWrongOrMalformedCodes() {
        long now = Totp.stepAt(Instant.ofEpochSecond(59));
        assertEquals(-1, Totp.match(RFC_SECRET, "000000", now, 1));
        assertEquals(-1, Totp.match(RFC_SECRET, "", now, 1));
        assertEquals(-1, Totp.match(RFC_SECRET, "28708", now, 1));
        assertEquals(-1, Totp.match(RFC_SECRET, "2870821", now, 1));
    }

    @Test
    void base32RoundTrips() {
        byte[] data = "HopHop secret 123".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(data, Base32.decode(Base32.encode(data)));
        assertArrayEquals(RFC_SECRET, Base32.decode("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"));
    }
}
