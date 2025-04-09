package com.albany.mvc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // List of paths that should be accessible without authentication
    private final List<String> publicPaths = Arrays.asList(
            "/admin/login",
            "/admin/api/login", // New API endpoint for login
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

        // Special case for dashboard with token parameter
        if (requestURI.equals("/admin/dashboard") && request.getParameter("token") != null) {
            log.debug("Allowing access to dashboard with token parameter");
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicPath) {
            // For public paths, just continue with the filter chain
            filterChain.doFilter(request, response);
            return;
        }

        // Check AJAX requests
        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        // Get JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            log.debug("Found JWT token in Authorization header");
        }

        // If token is valid, set authentication in context
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            Authentication auth = jwtUtil.getAuthentication(jwt);
            log.debug("Authentication successful. User: {}, Authorities: {}",
                    auth.getName(), auth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            // If token is invalid or missing, clear security context and handle accordingly
            SecurityContextHolder.clearContext();
            log.debug("Invalid or missing token. isAjaxRequest: {}", isAjaxRequest);

            // If it's an AJAX request, return 401 Unauthorized
            if (isAjaxRequest) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Session expired\",\"status\":401}");
            } else {
                // For regular requests, redirect to login page
                response.sendRedirect("/admin/login?error=session_expired");
            }
        }
    }
}