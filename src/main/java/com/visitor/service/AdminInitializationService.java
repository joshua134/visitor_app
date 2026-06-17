package com.visitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visitor.entity.User;
import com.visitor.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInitializationService {
    
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.super-admin.email}")
    private String adminEmail;
    
    @Value("${app.super-admin.first-name}")
    private String adminFirstName;
    
    @Value("${app.super-admin.last-name}")
    private String adminLastName;
    
    @Value("${app.super-admin.password}")
    private String adminPassword;
    
    @PostConstruct
    @Transactional
    public void initializeAdminUser() {
        log.info("Initializing super admin user...");
        
        try {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setName(adminFirstName+" "+adminLastName);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(roleService.getRole("ADMIN"));
                admin.setEnabled(true);
                admin.setLocked(false);
                admin.setForcePasswordChange(true);
                
                userRepository.save(admin);
                log.info("Super admin user created successfully with email: {}", adminEmail);
            } else {
                log.info("Super admin user already exists");
            }
        } catch (Exception e) {
            log.error("Failed to initialize admin user: {}", e.getMessage());
        }
    }
}