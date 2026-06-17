package com.visitor.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VisitorResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Integer age;
    private String nationalId;
    private String gender;
    private String phoneNumber;
    private String email;
    private String ipAddress;
    private String registrationDate;
}