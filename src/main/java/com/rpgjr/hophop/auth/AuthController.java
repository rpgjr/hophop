package com.rpgjr.hophop.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionStore sessions;

    @Value("${auth.setup-token:}")
    private String setupToken;

    @Value("${auth.issuer:HopHop}")
    private String issuer;

    public record LoginRequest(@NotBlank String code) {
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        AuthService.Authorization auth = authService.login(body.code(), clientOf(request));
        return Map.of("token", auth.token(), "idleSeconds", auth.idleSeconds());
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        sessions.revoke(AuthInterceptor.bearerToken(request));
    }

    /** Open, so a monitor or the host's health check can reach it without signing in. */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    /**
     * One page that shows the QR code to scan with Google Authenticator.
     * It exists only while AUTH_SETUP_TOKEN is set, so remove that variable after scanning.
     */
    @GetMapping(value = "/auth/setup", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> setup(@RequestParam(defaultValue = "") String token) {
        if (setupToken.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        boolean same = MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), setupToken.getBytes(StandardCharsets.UTF_8));
        if (!same) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!authService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Set AUTH_TOTP_SECRET first");
        }

        String secret = authService.secretBase32();
        String label = UriUtils.encode(issuer, StandardCharsets.UTF_8) + ":owner";
        String uri = "otpauth://totp/" + label + "?secret=" + secret
                + "&issuer=" + UriUtils.encode(issuer, StandardCharsets.UTF_8)
                + "&algorithm=SHA1&digits=6&period=30";

        String html = """
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>HopHop sign-in setup</title>
                <style>body{font:16px system-ui,sans-serif;max-width:420px;margin:48px auto;padding:0 16px;color:#0f172a}
                img{width:260px;height:260px;border:1px solid #e2e8f0;border-radius:12px}
                code{background:#f1f5f9;padding:2px 6px;border-radius:6px;word-break:break-all}</style></head>
                <body><h1>Scan with Google Authenticator</h1>
                <p>Open Google Authenticator, tap <b>+</b>, then <b>Scan a QR code</b>.</p>
                <img src="%s" alt="QR code for HopHop">
                <p>Cannot scan? Choose <b>Enter a setup key</b> and type:</p>
                <p><code>%s</code></p>
                <p>When it shows a 6-digit code, remove <code>AUTH_SETUP_TOKEN</code> from the server so this page goes away.</p>
                </body></html>
                """.formatted(QrCodes.pngDataUrl(uri, 260), HtmlUtils.htmlEscape(secret));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .body(html);
    }

    // The address the request came from. Behind Render's proxy that is the first X-Forwarded-For entry.
    private static String clientOf(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
