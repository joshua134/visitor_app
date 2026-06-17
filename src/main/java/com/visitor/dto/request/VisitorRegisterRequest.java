package com.visitor.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VisitorRegisterRequest {
	@NotBlank(message = "First name is required")
	private String firstName;

	@NotBlank(message = "Last name is required")
	private String lastName;

	@NotNull(message = "Date of birth is required")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfBirth;

	@NotBlank(message = "National ID/Passport is required")
	private String nationalId;

	@NotBlank(message = "Gender is required")
	@Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
	private String gender;

	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,20}$", message = "Please provide a valid phone number")
	private String phoneNumber;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;
	
	@NotBlank(message = "CAPTCHA is required")
    private String captcha;
}
