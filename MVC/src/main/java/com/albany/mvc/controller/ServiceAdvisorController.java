package com.albany.mvc.controller;

import com.albany.mvc.dto.ServiceAdvisorDto;
import com.albany.mvc.service.ServiceAdvisorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/service-advisors")
@RequiredArgsConstructor
@Slf4j
public class ServiceAdvisorController {

    private final ServiceAdvisorService serviceAdvisorService;

    @GetMapping
    public String serviceAdvisorsPage(
            @RequestParam(required = false) String token,
            Model model,
            HttpServletRequest request) {
        log.info("Accessing service advisors page");

        // Get token from various sources
        String validToken = getValidToken(token, request);

        if (validToken == null) {
            log.warn("No valid token found, redirecting to login");
            return "redirect:/admin/login?error=session_expired";
        }

        try {
            List<ServiceAdvisorDto> serviceAdvisors = serviceAdvisorService.getAllServiceAdvisors(validToken);
            model.addAttribute("serviceAdvisors", serviceAdvisors);
            log.info("Successfully loaded {} service advisors", serviceAdvisors.size());

            // Set the admin's name for the page
            model.addAttribute("userName", "Arthur Morgan");

            return "admin/serviceAdvisor";
        } catch (Exception e) {
            log.error("Error loading service advisors: {}", e.getMessage(), e);
            return "redirect:/admin/login?error=server_error";
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ServiceAdvisorDto> getServiceAdvisor(
            @PathVariable Integer id,
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        String validToken = getValidToken(token, request);

        if (validToken == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceAdvisorDto advisor = serviceAdvisorService.getServiceAdvisorById(id, validToken);

        if (advisor == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(advisor);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ServiceAdvisorDto> createServiceAdvisor(
            @RequestBody ServiceAdvisorDto advisorDto,
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        String validToken = getValidToken(token, request);

        if (validToken == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceAdvisorDto createdAdvisor = serviceAdvisorService.createServiceAdvisor(advisorDto, validToken);

        if (createdAdvisor == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(createdAdvisor);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ServiceAdvisorDto> updateServiceAdvisor(
            @PathVariable Integer id,
            @RequestBody ServiceAdvisorDto advisorDto,
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        String validToken = getValidToken(token, request);

        if (validToken == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceAdvisorDto updatedAdvisor = serviceAdvisorService.updateServiceAdvisor(id, advisorDto, validToken);

        if (updatedAdvisor == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(updatedAdvisor);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteServiceAdvisor(
            @PathVariable Integer id,
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        String validToken = getValidToken(token, request);

        if (validToken == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted = serviceAdvisorService.deleteServiceAdvisor(id, validToken);

        if (!deleted) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Gets a valid token from various sources
     */
    private String getValidToken(String tokenParam, HttpServletRequest request) {
        // Check parameter first
        if (tokenParam != null && !tokenParam.isEmpty()) {
            // Store token in session
            HttpSession session = request.getSession();
            session.setAttribute("jwt-token", tokenParam);
            return tokenParam;
        }

        // Check session next
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute("jwt-token");
            if (sessionToken != null && !sessionToken.isEmpty()) {
                return sessionToken;
            }
        }

        // Check header last
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}