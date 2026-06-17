package com.visitor.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String roleName;
    private Long roleId;
    private boolean enabled;
    private boolean accountNonLocked;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private String invitationStatus; // "PENDING", "ACCEPTED", "EXPIRED"
    private LocalDateTime invitationSentAt;
    private LocalDateTime invitationAcceptedAt;

}
