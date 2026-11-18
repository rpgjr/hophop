package com.rpgjr.hophop.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Lets a request through only when it carries a live session. It runs after CORS handling,
 * so a refused browser request still gets readable CORS headers.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final SessionStore sessions;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        if (!authService.isConfigured()) {
            return refuse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "auth-not-configured");
        }
        if (!sessions.touch(bearerToken(request))) {
            response.setHeader("WWW-Authenticate", "Bearer");
            return refuse(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized");
        }
        return true;
    }

    static String bearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) return header.substring(7).trim();
        return null;
    }

    private static boolean refuse(HttpServletResponse response, int status, String error) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + error + "\"}");
        return false;
    }
}
