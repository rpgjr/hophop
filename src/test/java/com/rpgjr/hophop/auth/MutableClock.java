package com.rpgjr.hophop.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock tests can move forward. */
class MutableClock extends Clock {

    private Instant now = Instant.parse("2026-11-16T00:00:00Z");

    void advance(Duration time) {
        now = now.plus(time);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now;
    }
}
