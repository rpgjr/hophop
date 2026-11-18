package com.rpgjr.hophop.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;

/**
 * Time-based one-time passwords (RFC 6238): 6 digits, 30 second steps, HMAC-SHA1.
 * These are the settings Google Authenticator uses.
 */
public final class Totp {

    public static final int STEP_SECONDS = 30;
    private static final int DIGITS = 6;

    private Totp() {
    }

    public static long stepAt(Instant time) {
        return time.getEpochSecond() / STEP_SECONDS;
    }

    public static String code(byte[] secret, long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            return String.format("%0" + DIGITS + "d", binary % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA1 is not available", e);
        }
    }

    /**
     * The step whose code equals the input, looking one step either side of now for clock drift.
     * Returns -1 when none match. Every candidate is compared, so timing reveals nothing.
     */
    public static long match(byte[] secret, String input, long nowStep, int window) {
        byte[] given = input.getBytes(StandardCharsets.UTF_8);
        long found = -1;
        for (long step = nowStep - window; step <= nowStep + window; step++) {
            byte[] expected = code(secret, step).getBytes(StandardCharsets.UTF_8);
            if (MessageDigest.isEqual(expected, given)) found = step;
        }
        return found;
    }
}
