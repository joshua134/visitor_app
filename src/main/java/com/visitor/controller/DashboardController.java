package com.visitor.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    @GetMapping("/dashboard")
    public String redirectBasedOnRole(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_VIEWER");
        
        switch (role) {
            case "ROLE_ADMIN":
                return "redirect:/admin/dashboard";
            case "ROLE_MANAGER":
                return "redirect:/manager/dashboard";
            case "ROLE_VIEWER":
                return "redirect:/viewer/dashboard";
            default:
                return "redirect:/admin/dashboard";
        }
    }
}