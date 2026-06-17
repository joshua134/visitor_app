package com.visitor.util;

import com.visitor.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetTokenStatus {
    private boolean valid;
    private boolean alreadyUsed;
    private boolean expired;
    private String message;
    private User user;
    
    public static ResetTokenStatus valid(User user) {
        return new ResetTokenStatus(true, false, false, null, user);
    }
    
    public static ResetTokenStatus alreadyUsed(String message) {
        return new ResetTokenStatus(false, true, false, message, null);
    }
    
    public static ResetTokenStatus expired(String message) {
        return new ResetTokenStatus(false, false, true, message, null);
    }
    
    public static ResetTokenStatus invalid(String message) {
        return new ResetTokenStatus(false, false, false, message, null);
    }
}