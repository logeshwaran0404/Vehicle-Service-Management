package com.albany.restapi.controller;

import com.albany.restapi.dto.ServiceAdvisorRequest;
import com.albany.restapi.dto.ServiceAdvisorResponse;
import com.albany.restapi.service.ServiceAdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-advisors")
@RequiredArgsConstructor
public class ServiceAdvisorController {

    private final ServiceAdvisorService advisorService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<List<ServiceAdvisorResponse>> getAllServiceAdvisors() {
        List<ServiceAdvisorResponse> advisors = advisorService.getAllServiceAdvisors();
        return ResponseEntity.ok(advisors);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ServiceAdvisorResponse> getServiceAdvisorById(@PathVariable Integer id) {
        ServiceAdvisorResponse advisor = advisorService.getServiceAdvisorById(id);
        return ResponseEntity.ok(advisor);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ServiceAdvisorResponse> createServiceAdvisor(@RequestBody ServiceAdvisorRequest request) {
        ServiceAdvisorResponse newAdvisor = advisorService.createServiceAdvisor(request);
        return ResponseEntity.ok(newAdvisor);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ServiceAdvisorResponse> updateServiceAdvisor(
            @PathVariable Integer id, 
            @RequestBody ServiceAdvisorRequest request) {
        ServiceAdvisorResponse updatedAdvisor = advisorService.updateServiceAdvisor(id, request);
        return ResponseEntity.ok(updatedAdvisor);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteServiceAdvisor(@PathVariable Integer id) {
        advisorService.deleteServiceAdvisor(id);
        return ResponseEntity.noContent().build();
    }
}