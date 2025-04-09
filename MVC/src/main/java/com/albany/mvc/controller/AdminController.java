package com.albany.mvc.controller;

import com.albany.mvc.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final JwtUtil jwtUtil;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String token, Model model, HttpServletResponse response) {
        // If we have a token parameter, let's use it to establish authentication for the session
        if (token != null && !token.isEmpty()) {
            log.info("Token provided in URL parameter, validating...");

            // Validate the token
            if (jwtUtil.validateToken(token)) {
                // Set the authentication in the security context
                Authentication tokenAuth = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(tokenAuth);

                log.info("Token validated and authentication set for user: {}", tokenAuth.getName());
            } else {
                log.warn("Invalid token provided in URL");
                return "redirect:/admin/login?error=invalid_token";
            }
        }

        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName(); // This will be the user's email

        log.info("Loading dashboard for user: {}", userName);

        // Add attributes to the model for the dashboard view
        model.addAttribute("userName", userName);

        // You can add more attributes for the dashboard stats here
        model.addAttribute("dashboardStats", null);

        return "admin/dashboard";
    }

//    @GetMapping("/customers")
//    public String customers(Model model) {
//        // Add any data needed for the customers view
//        model.addAttribute("customers", null);
//
//        return "admin/customers";
//    }

}