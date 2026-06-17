package com.visitor.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    
    @NotBlank(message = "Role name is required")
    private String name;
    
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer userCount;
}