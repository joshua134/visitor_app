package com.visitor.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.visitor.dto.request.ChangePasswordRequest;
import com.visitor.dto.request.ForceChangePasswordRequest;
import com.visitor.entity.User;
import com.visitor.service.CaptchaService;
import com.visitor.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordController {
	private final UserService userService;
    private final CaptchaService captchaService;
    
    @GetMapping("/force-change-password")
    public String forceChangePasswordForm(Model model, HttpSession session, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
        
        if (!user.isForcePasswordChange()) {
            log.info("User {} does not need to change password", user.getEmail());
            return "redirect:/dashboard";
        }
        
        if (!model.containsAttribute("changeRequest")) {
            model.addAttribute("changeRequest", new ForceChangePasswordRequest());
        }
        
        model.addAttribute("email", user.getEmail());
        model.addAttribute("name", user.getName());
        
        // Generate CAPTCHA
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        
        return "force-change-password";
    }
    
    @PostMapping("/force-change-password")
    public String forceChangePassword(@Valid ForceChangePasswordRequest changeRequest,
                                     BindingResult result,
                                     HttpSession session,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String email = authentication.getName();
        
        // Validate CAPTCHA
        String sessionCaptcha = (String) session.getAttribute("captcha_text");
        String userCaptcha = changeRequest.getCaptcha();
        
        if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha)) {
            result.rejectValue("captcha", "error.captcha", "Invalid CAPTCHA code. Please try again.");
            session.removeAttribute("captcha_text");
        }
        
        // Validate password match
        if (!changeRequest.getNewPassword().equals(changeRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }
        
        if (result.hasErrors()) {
            String newCaptchaText = captchaService.generateCaptchaText();
            session.setAttribute("captcha_text", newCaptchaText);
            
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changeRequest", result);
            redirectAttributes.addFlashAttribute("changeRequest", changeRequest);
            return "redirect:/force-change-password";
        }
        
        try {
            userService.forceChangePassword(email, changeRequest.getNewPassword());
            session.removeAttribute("captcha_text");
            
            // Logout the user and clear security context
            SecurityContextHolder.clearContext();
            session.invalidate();
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Password changed successfully! Please login with your new password.");
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/force-change-password";
        }
    }
    
    @GetMapping("/change-password")
    public String changePasswordForm(Model model, HttpSession session, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (!model.containsAttribute("changeRequest")) {
            model.addAttribute("changeRequest", new ChangePasswordRequest());
        }
        
        // Generate CAPTCHA
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        
        return "change-password";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@Valid ChangePasswordRequest changeRequest,
                                BindingResult result,
                                HttpSession session,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String email = authentication.getName();
        
        // Validate CAPTCHA
        String sessionCaptcha = (String) session.getAttribute("captcha_text");
        String userCaptcha = changeRequest.getCaptcha();
        
        if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha)) {
            result.rejectValue("captcha", "error.captcha", "Invalid CAPTCHA code. Please try again.");
            session.removeAttribute("captcha_text");
        }
        
        // Validate new password match
        if (!changeRequest.getNewPassword().equals(changeRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }
        
        if (result.hasErrors()) {
            String newCaptchaText = captchaService.generateCaptchaText();
            session.setAttribute("captcha_text", newCaptchaText);
            
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changeRequest", result);
            redirectAttributes.addFlashAttribute("changeRequest", changeRequest);
            return "redirect:/change-password";
        }
        
        try {
            userService.changePassword(email, changeRequest.getCurrentPassword(), changeRequest.getNewPassword());
            session.removeAttribute("captcha_text");
            
            // Logout and require re-login
            SecurityContextHolder.clearContext();
            session.invalidate();
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Password changed successfully! Please login with your new password.");
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/change-password";
        }
    }
}
