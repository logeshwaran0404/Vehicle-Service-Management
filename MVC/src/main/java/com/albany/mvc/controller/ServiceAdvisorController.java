package com.albany.mvc.controller;

import com.albany.mvc.dto.ServiceAdvisorDto;
import com.albany.mvc.service.ServiceAdvisorService;
import jakarta.servlet.http.HttpServletRequest;
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
    public String serviceAdvisorsPage(Model model, HttpServletRequest request) {
        // Get token from session or request header
        String token = extractToken(request);
        
        if (token == null) {
            return "redirect:/admin/login?error=session_expired";
        }
        
        List<ServiceAdvisorDto> serviceAdvisors = serviceAdvisorService.getAllServiceAdvisors(token);
        model.addAttribute("serviceAdvisors", serviceAdvisors);
        
        return "admin/serviceAdvisor";
    }
    
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ServiceAdvisorDto> getServiceAdvisor(@PathVariable Integer id, HttpServletRequest request) {
        String token = extractToken(request);
        
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        
        ServiceAdvisorDto advisor = serviceAdvisorService.getServiceAdvisorById(id, token);
        
        if (advisor == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(advisor);
    }
    
    @PostMapping
    @ResponseBody
    public ResponseEntity<ServiceAdvisorDto> createServiceAdvisor(
            @RequestBody ServiceAdvisorDto advisorDto, 
            HttpServletRequest request) {
        
        String token = extractToken(request);
        
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        
        ServiceAdvisorDto createdAdvisor = serviceAdvisorService.createServiceAdvisor(advisorDto, token);
        
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
            HttpServletRequest request) {
        
        String token = extractToken(request);
        
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        
        ServiceAdvisorDto updatedAdvisor = serviceAdvisorService.updateServiceAdvisor(id, advisorDto, token);
        
        if (updatedAdvisor == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(updatedAdvisor);
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteServiceAdvisor(
            @PathVariable Integer id,
            HttpServletRequest request) {
        
        String token = extractToken(request);
        
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        
        boolean deleted = serviceAdvisorService.deleteServiceAdvisor(id, token);
        
        if (!deleted) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.noContent().build();
    }
    
    private String extractToken(HttpServletRequest request) {
        // Check header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Check session - assuming token is stored in session as "jwt-token"
        Object tokenObj = request.getSession().getAttribute("jwt-token");
        if (tokenObj != null) {
            return tokenObj.toString();
        }
        
        return null;
    }
}