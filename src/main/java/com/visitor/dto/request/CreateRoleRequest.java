package com.visitor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
	@NotBlank(message = "Role name is required")
	@Pattern(regexp = "^[A-Z_]+$", message = "Role name must be uppercase with underscores (e.g., ADMIN, MANAGER, VIEWER)")
	private String name;

	@NotBlank(message = "Role description is required")
	private String description;
}
