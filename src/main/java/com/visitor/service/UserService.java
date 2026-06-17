package com.visitor.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visitor.dto.request.UserInvitationRequest;
import com.visitor.dto.response.UserResponse;
import com.visitor.entity.Role;
import com.visitor.entity.User;
import com.visitor.exception.NotFoundException;
import com.visitor.repository.RoleRepository;
import com.visitor.repository.UserRepository;
import com.visitor.util.InvitationStatus;
import com.visitor.util.ResetTokenStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    public InvitationStatus checkInvitationStatus(String token) {
        if (token == null || token.isEmpty()) {
            return InvitationStatus.invalid("Invalid invitation token");
        }
        
        Optional<User> userOpt = userRepository.findByInvitationToken(token);
        
        if (userOpt.isEmpty()) {
            return InvitationStatus.invalid("Invalid invitation token");
        }
        
        User user = userOpt.get();
        
        // Check if already accepted
        if (user.getInvitationAcceptedAt() != null && user.getInvitationToken() == null) {
            return InvitationStatus.alreadyAccepted("This invitation has already been accepted");
        }
        
        // Check if expired (48 hours)
        if (user.getInvitationSentAt().plusHours(48).isBefore(LocalDateTime.now())) {
            return InvitationStatus.expired("This invitation has expired (48 hours limit)");
        }
        
        // Valid invitation
        return InvitationStatus.valid(user);
    }
    
    @Transactional
    public UserResponse  inviteUser(UserInvitationRequest dto) throws Exception {
        // Check if user already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new Exception("User with this email already exists");
        }
        
        Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(() -> new NotFoundException("Role not found"));
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setRole(role);
        user.setEnabled(false);
        
        // Generate invitation token
        String token = UUID.randomUUID().toString();
        user.setInvitationToken(token);
        user.setInvitationSentAt(LocalDateTime.now());
        
        // Generate temporary password
//        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
//        user.setPassword(passwordEncoder.encode(tempPassword));
        
        User savedUser = userRepository.save(user);
        
        // Send invitation email
        String invitationLink = baseUrl + "/accept-invitation?token=" + token;
        emailService.sendInvitationEmail(dto.getEmail(), dto.getName(), invitationLink);
        
        log.info("Invitation sent to: {}", dto.getEmail());
        return convertToResponse(savedUser);
    }
    
    @Transactional
    public void forceChangePassword(String email, String newPassword) throws Exception {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new Exception("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        userRepository.save(user);
        
        log.info("User forced password change: {}", email);
    }
    
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) throws Exception {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed for user: {}", email);
    }
    
    @Transactional
    public UserResponse acceptInvitation(String token, String newPassword) throws Exception {
    	log.info("accepting the invitaiton ");
        User user = userRepository.findByInvitationToken(token).orElseThrow(() -> new NotFoundException("Invalid invitation token"));
        
        if (user.getInvitationAcceptedAt() != null && user.getInvitationToken() == null) {
            throw new Exception("Invitation already accepted");
        }
        
        // Check if invitation expired (48 hours)
        if (user.getInvitationSentAt().plusHours(48).isBefore(LocalDateTime.now())) {
            throw new Exception("Invitation has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setEnabled(true);
        user.setInvitationAcceptedAt(LocalDateTime.now());
        user.setInvitationToken(null);
        User updated  = userRepository.save(user);
        log.info("User accepted invitation: {}", user.getEmail());
        return convertToResponse(updated);
    }
    
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
    	 Page<User> usersPage = userRepository.findAll(pageable);
         return usersPage.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
    	 Page<User> usersPage = userRepository.searchUsers(search, pageable);
         return usersPage.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) throws Exception {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new Exception("User not found with id: " + id));
        return convertToResponse(user);
    }
    
    @Transactional
    public void disableUser(Long userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User disabled: {}", user.getEmail());
    }
    
    @Transactional
    public void enableUser(Long userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled: {}", user.getEmail());
    }
    
    @Transactional
    public UserResponse updateUserRole(Long userId, Long roleId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found"));
        
        user.setRole(role);
        User updatedUser =userRepository.save(user);
        log.info("User role updated: {} -> {}", user.getEmail(), role.getName());
        return convertToResponse(updatedUser);
    }
    
    @Transactional
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    @Transactional
    public void recordFailedLoginAttempt(String email) {
        userRepository.incrementFailedAttempts(email);
        
        // Lock user after 5 failed attempts
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
                user.setLockedAt(LocalDateTime.now());
                userRepository.save(user);
                log.warn("User account locked due to multiple failed attempts: {}", email);
            }
        });
    }
    
    @Transactional
    public void resetFailedAttempts(String email) {
        userRepository.resetFailedAttempts(email);
    }
    
    @Transactional
    public void initiatePasswordReset(String email) throws Exception {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new Exception("No account found with this email address"));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenSentAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Send reset email
        String resetLink = baseUrl + "/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, user.getName(), resetLink);
        
        log.info("Password reset initiated for email: {}", email);
    }
    
    public boolean needsPasswordChange(String email) {
        return userRepository.findByEmail(email)
            .map(User::isForcePasswordChange)
            .orElse(false);
    }
    
    public ResetTokenStatus checkResetTokenStatus(String token) {
        if (token == null || token.isEmpty()) {
            return ResetTokenStatus.invalid("Invalid reset token");
        }
        
        Optional<User> userOpt = userRepository.findByResetToken(token);
        
        if (userOpt.isEmpty()) {
            return ResetTokenStatus.invalid("Invalid reset token");
        }
        
        User user = userOpt.get();
        
        // Check if already used
        if (user.getResetTokenAcceptedAt() != null) {
            return ResetTokenStatus.alreadyUsed("This password reset link has already been used");
        }
        
        // Check if expired (1 hour)
        if (user.getResetTokenSentAt().plusHours(1).isBefore(LocalDateTime.now())) {
            return ResetTokenStatus.expired("This password reset link has expired (1 hour limit)");
        }
        
        // Valid token
        return ResetTokenStatus.valid(user);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        User user = userRepository.findByResetToken(token).orElseThrow(() -> new Exception("Invalid reset token"));
        
        if (user.getResetTokenAcceptedAt() != null) {
            throw new Exception("This password reset link has already been used");
        }
        
        // Check if expired (1 hour)
        if (user.getResetTokenSentAt().plusHours(1).isBefore(LocalDateTime.now())) {
            throw new Exception("This password reset link has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenAcceptedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
    
    private UserResponse convertToResponse(User user) {
        String invitationStatus = "NONE";
        if (user.getInvitationToken() != null && user.getInvitationAcceptedAt() == null) {
            // Check if expired
            if (user.getInvitationSentAt().plusHours(48).isBefore(LocalDateTime.now())) {
                invitationStatus = "EXPIRED";
            } else {
                invitationStatus = "PENDING";
            }
        } else if (user.getInvitationAcceptedAt() != null) {
            invitationStatus = "ACCEPTED";
        }
        
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .roleName(user.getRole().getName())
            .roleId(user.getRole().getId())
            .enabled(user.isEnabled())
            .accountNonLocked(!user.isLocked())
            .lastLogin(user.getLastLogin())
            .createdAt(user.getCreatedAt())
            .invitationStatus(invitationStatus)
            .invitationSentAt(user.getInvitationSentAt())
            .invitationAcceptedAt(user.getInvitationAcceptedAt())
            .build();
    }
}
