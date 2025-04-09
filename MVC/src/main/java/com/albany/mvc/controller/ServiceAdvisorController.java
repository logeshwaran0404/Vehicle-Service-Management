package com.albany.mvc.controller;

import com.albany.mvc.dto.ServiceAdvisorDto;
import com.albany.mvc.service.ServiceAdvisorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/service-advisors")
@RequiredArgsConstructor
@Slf4j
public class ServiceAdvisorController {

    private final ServiceAdvisorService serviceAdvisorService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.base-url}")
    private String apiBaseUrl;

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
            // Debug log the token
            log.debug("Using token: {}", validToken.substring(0, Math.min(10, validToken.length())) + "...");

            List<ServiceAdvisorDto> serviceAdvisors = serviceAdvisorService.getAllServiceAdvisors(validToken);
            model.addAttribute("serviceAdvisors", serviceAdvisors);
            log.info("Successfully loaded {} service advisors", serviceAdvisors.size());

            // Set the admin's name for the page (hardcoded per requirement)
            model.addAttribute("userName", "Arthur Morgan");

            return "admin/serviceAdvisor";
        } catch (Exception e) {
            log.error("Error loading service advisors: {}", e.getMessage(), e);
            model.addAttribute("apiError", "Failed to load service advisors: " + e.getMessage());
            return "admin/serviceAdvisor"; // Return the page with error message
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

        try {
            ServiceAdvisorDto advisor = serviceAdvisorService.getServiceAdvisorById(id, validToken);

            if (advisor == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(advisor);
        } catch (Exception e) {
            log.error("Error getting service advisor: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
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

        try {
            ServiceAdvisorDto createdAdvisor = serviceAdvisorService.createServiceAdvisor(advisorDto, validToken);

            if (createdAdvisor == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(createdAdvisor);
        } catch (Exception e) {
            log.error("Error creating service advisor: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
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

        try {
            ServiceAdvisorDto updatedAdvisor = serviceAdvisorService.updateServiceAdvisor(id, advisorDto, validToken);

            if (updatedAdvisor == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(updatedAdvisor);
        } catch (Exception e) {
            log.error("Error updating service advisor: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
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

        try {
            boolean deleted = serviceAdvisorService.deleteServiceAdvisor(id, validToken);

            if (!deleted) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting service advisor: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/debug-token")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugToken(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        String validToken = getValidToken(token, request);
        Map<String, Object> response = new HashMap<>();

        if (validToken == null) {
            response.put("error", "No valid token found");
            return ResponseEntity.status(401).body(response);
        }

        response.put("token_valid", true);
        response.put("token_prefix", validToken.substring(0, Math.min(10, validToken.length())) + "...");

        // Test API connection
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(validToken);

            ResponseEntity<String> testResponse = restTemplate.exchange(
                    apiBaseUrl + "/debug/token-info",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            response.put("api_connection", "success");
            response.put("api_status", testResponse.getStatusCode().toString());

            if (testResponse.getBody() != null) {
                response.put("api_response", objectMapper.readValue(testResponse.getBody(), Map.class));
            }
        } catch (Exception e) {
            response.put("api_connection", "failed");
            response.put("error_message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Gets a valid token from various sources
     */
    private String getValidToken(String tokenParam, HttpServletRequest request) {
        // Check parameter first
        if (tokenParam != null && !tokenParam.isEmpty()) {
            log.debug("Using token from parameter");
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
                log.debug("Using token from session");
                return sessionToken;
            }
        }

        // Check header last
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Using token from Authorization header");
            return authHeader.substring(7);
        }

        log.warn("No valid token found from any source");
        return null;
    }
}