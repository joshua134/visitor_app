package com.visitor.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.visitor.util.EncryptionUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_visitor", indexes= {
		@Index(name = "idx_email", columnList = "email_encrypted"),
	    @Index(name = "idx_phone", columnList = "phone_encrypted"),
	    @Index(name = "idx_national_id", columnList = "national_id_encrypted"),
	    @Index(name = "idx_first_name", columnList = "firstName"),
	    @Index(name = "idx_last_name", columnList = "lastName"),
		 @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="first_name",nullable = false)
    @NotBlank(message = "First name is required")
    @Convert(converter = EncryptionUtil.class)
    private String firstName;
    
    @Column(name="last_name",nullable = false)
    @NotBlank(message = "Last name is required")
    @Convert(converter = EncryptionUtil.class)
    private String lastName;
    
    @Column(name="date_of_birth",nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name="national_id",nullable = false)
    @NotBlank(message = "National ID/Passport is required")
    @Convert(converter = EncryptionUtil.class)
    private String nationalId;
    
    @Column(name="gender",nullable = false)
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @Column(name="phone_number", nullable = false)
//    @Pattern(regexp = "^\\+?\\d{10,20}$", message = "Invalid phone number")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,20}$", message = "Invalid phone number")
    @Convert(converter = EncryptionUtil.class)
    private String phoneNumber;
    
    @Column(name="email", nullable = false)
    @Email(message = "Invalid email")
    @Convert(converter = EncryptionUtil.class)
    private String email;
    
    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt;
    
    private String additionalDetails;
    
    @Column(name="ip_address",nullable = false)
    private String ipAddress;
    
    @Column(name="user_agent")
    private String userAgent;
  
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}