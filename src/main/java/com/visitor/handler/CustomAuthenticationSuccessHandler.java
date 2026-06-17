package com.visitor.handler;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.visitor.entity.User;
import com.visitor.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElse(null);
        
        if(user != null) {
        	if(user.isForcePasswordChange()) {
        		log.info("User {} required to change password on first login", email);
        		 response.sendRedirect("/force-change-password");
                 return;
        	}
        	
        	// Update last login time
            user.setLastLogin(LocalDateTime.now());
            user.setFailedAttempts(0);
            userRepository.save(user);
        	
            // Get role and redirect accordingly
            String role = user.getRole().getName();
            
            switch (role) {
            case "ADMIN":
            	log.info("Admin user logged in: {}", email);
            	response.sendRedirect("/admin/dashboard");
            	break;
            case "MANAGER":
            	log.info("Manager user logged in: {}", email);
            	response.sendRedirect("/manager/dashboard");
            	break;
            case "VIEWER":
            	log.info("Viewer user logged in: {}", email);
            	response.sendRedirect("/viewer/dashboard");
            	break;
            default:
            	response.sendRedirect("/dashboard");
            	break;
            }
        } else {
            response.sendRedirect("/dashboard");
        } 
    }
}