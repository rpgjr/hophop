package com.rpgjr.hophop.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static com.rpgjr.hophop.utils.Constants.SCRAPER_INVALID_SITE_URL;
import static com.rpgjr.hophop.utils.Constants.WWW_DOT;

public final class HostUtils {

    private HostUtils() {
    }

    // "https://www.JobSite.com/jobs/1" becomes "jobsite.com".
    public static String hostOf(String url) {
        try {
            String host = URI.create(url.trim()).getHost();
            if (host == null) throw new IllegalArgumentException();
            return normalizeHost(host);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SCRAPER_INVALID_SITE_URL);
        }
    }

    public static String normalizeHost(String host) {
        String lower = host.trim().toLowerCase();
        return lower.startsWith(WWW_DOT) ? lower.substring(4) : lower;
    }

}
