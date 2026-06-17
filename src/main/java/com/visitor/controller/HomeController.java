package com.visitor.controller;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.visitor.dto.request.AcceptInvitationRequest;
import com.visitor.dto.request.ForgotPasswordRequest;
import com.visitor.dto.request.ResetPasswordRequest;
import com.visitor.dto.request.VisitorRegisterRequest;
import com.visitor.dto.response.VisitorResponse;
import com.visitor.service.CaptchaService;
import com.visitor.service.UserService;
import com.visitor.service.VisitorService;
import com.visitor.util.InvitationStatus;
import com.visitor.util.ResetTokenStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
	private final VisitorService visitorService;
	private final CaptchaService captchaService;
	private final UserService userService;

	@GetMapping("/")
	public String index(Model model) {
		if (!model.containsAttribute("visitor")) {
			model.addAttribute("visitor", new VisitorRegisterRequest());
		}
		return "index";
	}
	
	@GetMapping("/captcha-image")
    public void captchaImage(HttpSession session, HttpServletResponse response) throws Exception {
        // Generate CAPTCHA text and store in session
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        
        // Generate image
        BufferedImage image = captchaService.generateCaptchaImage(captchaText);
        
        // Set response headers
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // Write image to response
        ImageIO.write(image, "png", response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
    
    @GetMapping("/refresh-captcha")
    public void refreshCaptcha(HttpSession session, HttpServletResponse response) throws Exception {
        // Generate new CAPTCHA
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        
        // Generate image
        BufferedImage image = captchaService.generateCaptchaImage(captchaText);
        
        // Set response headers
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // Write image to response
        ImageIO.write(image, "png", response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model, HttpSession session) {
        if (!model.containsAttribute("forgotRequest")) {
            model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        }
        
        // Generate CAPTCHA
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid ForgotPasswordRequest forgotRequest,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        // Validate CAPTCHA
        String sessionCaptcha = (String) session.getAttribute("captcha_text");
        String userCaptcha = forgotRequest.getCaptcha();
        
        if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha)) {
            result.rejectValue("captcha", "error.captcha", "Invalid CAPTCHA code. Please try again.");
            session.removeAttribute("captcha_text");
        }
        
        if (result.hasErrors()) {
            // Generate new CAPTCHA
            String newCaptchaText = captchaService.generateCaptchaText();
            session.setAttribute("captcha_text", newCaptchaText);
            
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.forgotRequest", result);
            redirectAttributes.addFlashAttribute("forgotRequest", forgotRequest);
            return "redirect:/forgot-password";
        }
        
        try {
            userService.initiatePasswordReset(forgotRequest.getEmail());
            session.removeAttribute("captcha_text");
            redirectAttributes.addFlashAttribute("successMessage", 
                "If an account exists with this email, you will receive a password reset link shortly.");
            return "redirect:/login";
        } catch (Exception e) {
            // Don't reveal if email exists or not for security
            redirectAttributes.addFlashAttribute("successMessage", 
                "If an account exists with this email, you will receive a password reset link shortly.");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model, HttpSession session) {
        // Validate the reset token
        ResetTokenStatus status = userService.checkResetTokenStatus(token);
        
        if (!status.isValid()) {
            model.addAttribute("error", status.getMessage());
            model.addAttribute("showForm", false);
            
            if (status.isAlreadyUsed()) {
                model.addAttribute("errorTitle", "Link Already Used");
                model.addAttribute("errorAction", "This password reset link has already been used");
                model.addAttribute("actionLink", "/login");
                model.addAttribute("actionText", "Go to Login");
            } else if (status.isExpired()) {
                model.addAttribute("errorTitle", "Link Expired");
                model.addAttribute("errorAction", "Please request a new password reset link");
                model.addAttribute("actionLink", "/forgot-password");
                model.addAttribute("actionText", "Request New Link");
            } else {
                model.addAttribute("errorTitle", "Invalid Link");
                model.addAttribute("errorAction", "The password reset link is invalid");
                model.addAttribute("actionLink", "/forgot-password");
                model.addAttribute("actionText", "Request New Link");
            }
            
            return "reset-password";
        }
        
        // Valid token - show the form
        model.addAttribute("token", token);
        model.addAttribute("showForm", true);
        model.addAttribute("userEmail", status.getUser().getEmail());
        model.addAttribute("userName", status.getUser().getName());
        
        if (!model.containsAttribute("resetRequest")) {
            model.addAttribute("resetRequest", new ResetPasswordRequest());
        }
        
        // Generate CAPTCHA
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                               @Valid ResetPasswordRequest resetRequest,
                               BindingResult result,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        // Validate CAPTCHA
        String sessionCaptcha = (String) session.getAttribute("captcha_text");
        String userCaptcha = resetRequest.getCaptcha();
        
        if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha)) {
            result.rejectValue("captcha", "error.captcha", "Invalid CAPTCHA code. Please try again.");
            session.removeAttribute("captcha_text");
        }
        
        // Validate password match
        if (!resetRequest.getPassword().equals(resetRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }
        
        if (result.hasErrors()) {
            // Generate new CAPTCHA
            String newCaptchaText = captchaService.generateCaptchaText();
            session.setAttribute("captcha_text", newCaptchaText);
            
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.resetRequest", result);
            redirectAttributes.addFlashAttribute("resetRequest", resetRequest);
            return "redirect:/reset-password?token=" + token;
        }
        
        try {
            userService.resetPassword(token, resetRequest.getPassword());
            session.removeAttribute("captcha_text");
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully! You can now login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
    
	@PostMapping("/register")
	public String registerVisitor(@Valid VisitorRegisterRequest visitorDTO, HttpSession session,
			BindingResult result,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		
		// Validate CAPTCHA
        String sessionCaptcha = (String) session.getAttribute("captcha_text");
        String userCaptcha = visitorDTO.getCaptcha();
        
        if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha)) {
            result.rejectValue("captcha", "error.captcha", "Invalid CAPTCHA code. Please try again.");
            session.removeAttribute("captcha_text");
            
            // Add flash attributes to preserve form data
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.visitor", result);
            redirectAttributes.addFlashAttribute("visitor", visitorDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid CAPTCHA code. Please try again.");
            return "redirect:/#register";
        }
		
        if (result.hasErrors()) {
			session.removeAttribute("captcha_text");
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.visitor", result);
			redirectAttributes.addFlashAttribute("visitor", visitorDTO);
			return "redirect:/#register";
		}

		try {
			VisitorResponse registeredVisitor = visitorService.registerVisitor(visitorDTO, request);
			session.removeAttribute("captcha_text");
			redirectAttributes.addFlashAttribute("successMessage", 
					"Registration successful! A confirmation email has been sent to " + registeredVisitor.getEmail());
			return "redirect:/register-success";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			session.removeAttribute("captcha_text");
			return "redirect:/#register";
		}
	}

	@GetMapping("/register-success")
	public String registrationSuccess() {
		return "register-success";
	}
	
	@GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           @RequestParam(value = "success", required = false) String success,
                           @ModelAttribute("successMessage") String successMessage, 
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (success != null) {
            model.addAttribute("success", success); 
        }
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("success", successMessage);
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }
	
	@GetMapping("/accept-invitation")
    public String acceptInvitationForm(@RequestParam("token") String token, Model model, HttpSession session) {
//        model.addAttribute("token", token);
        
        InvitationStatus status = userService.checkInvitationStatus(token);
        
        if (!status.isValid()) {
            model.addAttribute("error", status.getMessage());
            model.addAttribute("showForm", false);
            
            if (status.isAlreadyAccepted()) {
                model.addAttribute("errorTitle", "Invitation Already Accepted");
                model.addAttribute("errorAction", "Please login to your account");
                model.addAttribute("actionLink", "/login");
                model.addAttribute("actionText", "Go to Login");
            } else if (status.isExpired()) {
                model.addAttribute("errorTitle", "Invitation Expired");
                model.addAttribute("errorAction", "Please contact the administrator for a new invitation");
                model.addAttribute("actionLink", "/");
                model.addAttribute("actionText", "Go to Homepage");
            } else {
                model.addAttribute("errorTitle", "Invalid Invitation");
                model.addAttribute("errorAction", "Please check your invitation link or contact support");
                model.addAttribute("actionLink", "/");
                model.addAttribute("actionText", "Go to Homepage");
            }
            
            return "accept-invitation";
        }
        
        model.addAttribute("token", token);
        model.addAttribute("showForm", true);
        model.addAttribute("userEmail", status.getUser().getEmail());
        model.addAttribute("userName", status.getUser().getName());
        
        if(!model.containsAttribute("acceptRequest")) {
        	model.addAttribute("acceptRequest", new AcceptInvitationRequest());
        }
        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("captcha_text", captchaText);
        return "accept-invitation";
    }
    
    @PostMapping("/accept-invitation")
    public String acceptInvitation(@RequestParam("token") String token,@Valid AcceptInvitationRequest acceptRequest,BindingResult result,
                                  RedirectAttributes redirectAttributes, HttpSession session) {
    	String sessionCaptcha = (String) session.getAttribute("captcha_text");
        String userCaptcha = acceptRequest.getCaptcha();
        
        if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha)) {
            result.rejectValue("captcha", "error.captcha", "Invalid CAPTCHA code. Please try again.");
            session.removeAttribute("captcha_text");
        }
    	
        if (acceptRequest.getPassword() != null && acceptRequest.getConfirmPassword() != null) {
            if (!acceptRequest.getPassword().equals(acceptRequest.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            }
        }
    	
//    	if (result.hasErrors()) {
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.acceptRequest", result);
//            redirectAttributes.addFlashAttribute("acceptRequest", acceptRequest);
//            redirectAttributes.addFlashAttribute("error", "Please correct the errors below");
//            return "redirect:/accept-invitation?token=" + token;
//        }
        
        if (result.hasErrors()) {
            // Log the errors for debugging
            log.error("Validation errors: {}", result.getAllErrors());
            
            // Generate new CAPTCHA for the retry
            String newCaptchaText = captchaService.generateCaptchaText();
            session.setAttribute("captcha_text", newCaptchaText);
            
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.acceptRequest", result);
            redirectAttributes.addFlashAttribute("acceptRequest", acceptRequest);
            redirectAttributes.addFlashAttribute("error", "Please correct the errors below");
            return "redirect:/accept-invitation?token=" + token;
        }
        
    	try {
            userService.acceptInvitation(token, acceptRequest.getPassword());
            log.info("User accepted invitation with token: {}", token);
            session.removeAttribute("captcha_text");
            redirectAttributes.addFlashAttribute("successMessage", "Invitation accepted successfully! You can now login.");
            return "redirect:/login";
        } catch (Exception e) {
        	log.error("Error accepting invitation: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            session.removeAttribute("captcha_text");
            return "redirect:/accept-invitation?token=" + token;
        }
    }
    
}
