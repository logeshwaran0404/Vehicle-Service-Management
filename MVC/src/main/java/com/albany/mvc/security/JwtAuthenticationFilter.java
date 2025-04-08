package com.albany.mvc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${jwt.cookie-name}")
    private String jwtCookieName;

    // List of paths that should be accessible without authentication
    private final List<String> publicPaths = Arrays.asList(
            "/admin/login",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico",
            "/error",
            "/test-auth"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Check if the request is for a public path
        String requestURI = request.getRequestURI();
        boolean isPublicPath = publicPaths.stream().anyMatch(requestURI::startsWith);

        if (isPublicPath) {
            // For public paths, just continue with the filter chain
            filterChain.doFilter(request, response);
            return;
        }

        // Get JWT token from cookie
        String jwt = extractJwtFromCookies(request);

        // If token is valid, set authentication in context
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            Authentication auth = jwtUtil.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            // If token is invalid or missing, clear security context and redirect to login
            SecurityContextHolder.clearContext();

            // Delete the invalid token cookie
            if (jwt != null) {
                Cookie cookie = new Cookie(jwtCookieName, null);
                cookie.setMaxAge(0); // Delete the cookie
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            response.sendRedirect("/admin/login?error=session_expired");
        }
    }

    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(jwtCookieName))
                .findFirst();

        return jwtCookie.map(Cookie::getValue).orElse(null);
    }
}