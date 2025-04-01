package com.albany.mvc.service;

import com.albany.mvc.dto.AuthRequest;
import com.albany.mvc.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final RestTemplate restTemplate;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    public AuthResponse authenticate(AuthRequest request) {
        String url = apiBaseUrl + "/auth/login";
        log.info("Attempting to authenticate user: {} with API endpoint: {}", request.getEmail(), url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.debug("Sending authentication request to API");
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AuthResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Authentication successful for user: {}", request.getEmail());
                return response.getBody();
            } else {
                log.error("API returned success but with null or invalid body");
                throw new RuntimeException("Invalid response from authentication server");
            }
        } catch (HttpClientErrorException e) {
            log.error("API returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RuntimeException("Invalid credentials");
            }
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error communicating with API: {}", e.getMessage());
            throw new RuntimeException("Error connecting to authentication server: " + e.getMessage());
        }
    }
}