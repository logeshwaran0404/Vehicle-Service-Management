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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    
    @Value("${jwt.cookie-name}")
    private String jwtCookieName;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Skip filter for login page and static resources
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/admin/login") || 
            requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") ||
            requestURI.equals("/favicon.ico") ||
            requestURI.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get JWT token from cookie
        String jwt = extractJwtFromCookies(request);
        
        // If token is valid, set authentication in context
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            Authentication auth = jwtUtil.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (jwt != null) {
            // If token is invalid, clear security context and redirect to login
            SecurityContextHolder.clearContext();
            response.sendRedirect("/admin/login?error=session_expired");
            return;
        }
        
        filterChain.doFilter(request, response);
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