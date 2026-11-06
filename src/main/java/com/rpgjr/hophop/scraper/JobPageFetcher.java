package com.rpgjr.hophop.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static com.rpgjr.hophop.utils.Constants.*;

@Component
public class JobPageFetcher {

    private static final int MAX_CHARS = 12_000;

    // downloads the page using Jsoup and return it as a document
    public Document fetch(String url) {
        checkPublicUrl(url);
        try {
            return Jsoup.connect(url)
                    .userAgent(SCRAPER_USER_AGENT)
                    .timeout(10_000)
                    .get();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, SCRAPER_DOCUMENT_ERROR);
        }
    }


    // Page text for the AI: JSON-LD first, then the visible text, cut to MAX_CHARS.
    public String toText(Document page) {
        StringBuilder text = new StringBuilder();
        page.select(SCRAPER_CSS_QUERY)
                .forEach(script -> text.append(script.data()).append("\n"));
        text.append(page.title()).append("\n").append(page.body().text());

        return text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text.toString();
    }

    // checks the url and only allows http and https url
    private void checkPublicUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !(HTTP_NAME.equals(scheme) || HTTPS_NAME.equals(scheme))) {
                throw new IllegalArgumentException();
            }
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                    throw new IllegalArgumentException();
                }
            }
        } catch (IllegalArgumentException | UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SCRAPER_URL_NOT_VALID);
        }
    }
}