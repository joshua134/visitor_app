package com.visitor.util;

import com.visitor.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitationStatus {
	private boolean valid;
    private boolean alreadyAccepted;
    private boolean expired;
    private String message;
    private User user;
    
    public static InvitationStatus valid(User user) {
        return new InvitationStatus(true, false, false, null, user);
    }
    
    public static InvitationStatus alreadyAccepted(String message) {
        return new InvitationStatus(false, true, false, message, null);
    }
    
    public static InvitationStatus expired(String message) {
        return new InvitationStatus(false, false, true, message, null);
    }
    
    public static InvitationStatus invalid(String message) {
        return new InvitationStatus(false, false, false, message, null);
    }
}
