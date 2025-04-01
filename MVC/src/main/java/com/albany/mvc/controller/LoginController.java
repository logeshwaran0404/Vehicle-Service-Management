package com.albany.mvc.controller;

import com.albany.mvc.dto.AuthRequest;
import com.albany.mvc.dto.AuthResponse;
import com.albany.mvc.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
            Model model,
            HttpServletRequest request
    ) {
        log.info("Login page accessed, error param: {}, logout param: {}", error, logout);

        // Check if already authenticated
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(jwtCookieName)) {
                    log.info("User already has auth token, redirecting to dashboard");
                    return "redirect:/admin/dashboard";
                }
            }
        }

        if (error != null) {
            String errorMessage;
            switch (error) {
                case "session_expired":
                    errorMessage = "Your session has expired. Please log in again.";
                    break;
                case "unauthorized":
                    errorMessage = "You do not have permission to access this resource.";
                    break;
                case "invalid_credentials":
                    errorMessage = "Invalid email or password. Please check your credentials.";
                    break;
                case "server_error":
                    errorMessage = "Could not connect to authentication server. Please try again later.";
                    break;
                default:
                    errorMessage = "Authentication failed. Please verify your email and password.";
                    break;
            }
            model.addAttribute("error", errorMessage);
            log.warn("Login page showing error: {}", errorMessage);
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
            log.info("User logged out, showing success message");
        }

        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletResponse response,
            Model model
    ) {
        log.info("Processing login request for email: {}", email);

        try {
            // Create auth request
            AuthRequest authRequest = new AuthRequest();
            authRequest.setEmail(email);
            authRequest.setPassword(password);

            // Call API to authenticate user
            AuthResponse authResponse = authService.authenticate(authRequest);

            log.info("Authentication response received: {}", authResponse);

            // Check if user is an admin
            String role = authResponse.getRole();
            if (role != null) {
                role = role.toUpperCase().replace("\"", "").trim();
            }

            log.info("User role: {}", role);

            if (!"ADMIN".equals(role)) {
                log.warn("Non-admin user attempted to access admin portal: {}, role: {}",
                        email, role);
                model.addAttribute("error", "You do not have permission to access the admin portal");
                return "admin/login";
            }

            log.info("Admin login successful for: {}", email);

            // Store token in cookie
            Cookie cookie = new Cookie(jwtCookieName, authResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            // Redirect to dashboard
            return "redirect:/admin/dashboard";

        } catch (BadCredentialsException e) {
            log.error("Bad credentials: {}", e.getMessage());
            model.addAttribute("error", "Invalid email or password. Please try again.");
            return "admin/login";
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("API authentication rejected: {}", e.getMessage());
                model.addAttribute("error", "Invalid email or password. Please try again.");
            } else {
                log.error("API error: {} - {}", e.getStatusCode(), e.getMessage());
                model.addAttribute("error", "Authentication service error: " + e.getStatusCode());
            }
            return "admin/login";
        } catch (RestClientException e) {
            log.error("API connection error: {}", e.getMessage());
            model.addAttribute("error", "Unable to connect to authentication server. Please try again later.");
            return "admin/login";
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
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