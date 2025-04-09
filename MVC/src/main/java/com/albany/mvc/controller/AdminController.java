package com.albany.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName(); // This will be the user's email

        log.info("Loading dashboard for user: {}", userName);

        // Add attributes to the model for the dashboard view
        model.addAttribute("userName", userName);

        // You can add more attributes for the dashboard stats here
        // For now we're just passing a placeholder
        model.addAttribute("dashboardStats", null);

        return "admin/dashboard";
    }
    
    @GetMapping("/customers")
    public String customers(Model model) {
        // Add any data needed for the customers view
        model.addAttribute("customers", null);
        
        return "admin/customers";
    }
    
    @GetMapping("/service-advisors")
    public String serviceAdvisors(Model model) {
        // Add any data needed for the service advisors view
        model.addAttribute("serviceAdvisors", null);
        
        return "admin/serviceAdvisor";
    }
}