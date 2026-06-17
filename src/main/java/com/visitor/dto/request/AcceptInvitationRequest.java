package com.visitor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInvitationRequest {
	@NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotNull(message = "Confirm Password is required")
    @NotBlank(message = "Confirm Password cannot be blank")
    @Size(min = 8, message = "Confirm Password must be at least 8 characters")
    private String confirmPassword;
    
    @NotBlank(message = "CAPTCHA is required")
    private String captcha;
}
