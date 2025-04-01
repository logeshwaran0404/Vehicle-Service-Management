package com.albany.mvc.controller;

import com.albany.mvc.dto.AuthRequest;
import com.albany.mvc.dto.AuthResponse;
import com.albany.mvc.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthenticationService authService;

    @Value("${jwt.cookie-name}")
    private String jwtCookieName;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null) {
            if (error.equals("session_expired")) {
                model.addAttribute("error", "Your session has expired. Please log in again.");
            } else if (error.equals("unauthorized")) {
                model.addAttribute("error", "You do not have permission to access this resource.");
            } else if (error.equals("invalid_grant")) {
                model.addAttribute("error", "Invalid email or password. Please check your credentials.");
            } else if (error.equals("server_error")) {
                model.addAttribute("error", "Could not connect to authentication server. Please try again later.");
            } else {
                model.addAttribute("error", "Authentication failed. Please verify your email and password.");
            }
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @ModelAttribute AuthRequest authRequest,
            HttpServletResponse response,
            Model model
    ) {
        log.info("Processing login request for email: {}", authRequest.getEmail());

        try {
            // Call API to authenticate user
            AuthResponse authResponse = authService.authenticate(authRequest);

            // Check if user is an admin
            if (!"ADMIN".equals(authResponse.getRole())) {
                log.warn("Non-admin user attempted to access admin portal: {}", authRequest.getEmail());
                model.addAttribute("error", "You do not have permission to access the admin portal");
                return "admin/login";
            }

            log.info("Admin login successful for: {}", authRequest.getEmail());

            // Store token in cookie
            Cookie cookie = new Cookie(jwtCookieName, authResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            // Redirect to dashboard
            return "redirect:/admin/dashboard";

        } catch (BadCredentialsException | HttpClientErrorException e) {
            log.error("Authentication failed: {}", e.getMessage());
            model.addAttribute("error", "Invalid email or password. Please try again.");
            return "admin/login";
        } catch (RestClientException e) {
            log.error("API connection error: {}", e.getMessage());
            model.addAttribute("error", "Unable to connect to authentication server. Please try again later.");
            return "admin/login";
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage());
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
            return "admin/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie cookie = new Cookie(jwtCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        log.info("User logged out successfully");
        return "redirect:/admin/login?logout=true";
    }
}