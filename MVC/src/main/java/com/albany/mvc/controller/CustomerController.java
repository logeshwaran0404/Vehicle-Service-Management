package com.albany.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    @PostMapping
    public ResponseEntity<?> createCustomer(
            @RequestBody Map<String, Object> customerData,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Creating customer: {}", customerData);
        
        try {
            // Forward the request to the backend API
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(customerData, headers);
            
            // Make the API call to the backend
            ResponseEntity<Object> response = restTemplate.exchange(
                    apiBaseUrl + "/customers",
                    HttpMethod.POST,
                    entity,
                    Object.class
            );
            
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create customer: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllCustomers(@RequestHeader("Authorization") String authHeader) {
        try {
            // Forward the request to the backend API
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            // Make the API call to the backend
            ResponseEntity<Object> response = restTemplate.exchange(
                    apiBaseUrl + "/customers",
                    HttpMethod.GET,
                    entity,
                    Object.class
            );
            
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            
        } catch (Exception e) {
            log.error("Error fetching customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch customers: " + e.getMessage()));
        }
    }
}