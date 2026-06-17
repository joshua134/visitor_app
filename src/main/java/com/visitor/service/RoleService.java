package com.visitor.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visitor.dto.request.CreateRoleRequest;
import com.visitor.dto.response.RoleResponse;
import com.visitor.entity.Role;
import com.visitor.exception.NotFoundException;
import com.visitor.repository.RoleRepository;
import com.visitor.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
//    @PostConstruct
//    @Transactional
//    public void initializeRoles() {
//        log.info("Initializing default roles...");
//        
//        List<Role> defaultRoles = Arrays.asList(
//        		createRoleObject("ADMIN", "Full system access - can manage users, visitors, and system settings")
//        		createRoleObject("MANAGER", "Can view and manage visitors, but cannot manage users"),
//        		createRoleObject("VIEWER", "Read-only access to view visitors")
//        );
//        
//        for (Role role : defaultRoles) {
//            if (!roleRepository.existsByName(role.getName())) {
//                roleRepository.save(role);
//                log.info("Created role: {}", role.getName());
//            }
//        }
//        
//        log.info("Default roles initialization completed");
//    }
     
    private Role createRoleObject(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return role;
    }
    
    public List<RoleResponse> getAllRoles() {
    	List<Role> roles = roleRepository.findAll();
        return roles.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) throws NotFoundException {
        Role role = roleRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException("Role not found with name: " + name));
        return convertToResponse(role);
    }
    
    @Transactional(readOnly = true)
    public Role getRole(String name) throws NotFoundException {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException("Role not found with name: " + name));
    }
    
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) throws Exception {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new Exception("Role not found with id: " + id));
        return convertToResponse(role);
    }
    
    @Transactional
    public RoleResponse createRole(CreateRoleRequest dto) throws Exception {
        // Check if role already exists
        if (roleRepository.existsByName(dto.getName())) {
            throw new Exception("Role with name " + dto.getName() + " already exists");
        }
        
        // Validate role name format
        if (!dto.getName().matches("^[A-Z_]+$")) {
            throw new Exception("Role name must be uppercase with underscores only (e.g., SUPER_ADMIN, DATA_ENTRY)");
        }
        
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        
        Role savedRole = roleRepository.save(role);
        log.info("New role created: {} - {}", savedRole.getName(), savedRole.getDescription());
        
        return convertToResponse(savedRole);
    }
    
    @Transactional
    public RoleResponse updateRole(Long id, CreateRoleRequest dto) throws Exception {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new Exception("Role not found with id: " + id));
        
        // Check if trying to change to an existing role name
        if (!role.getName().equals(dto.getName()) && roleRepository.existsByName(dto.getName())) {
            throw new Exception("Role with name " + dto.getName() + " already exists");
        }
        
        // Prevent modification of critical roles
        if (role.getName().equals("ADMIN") && !dto.getName().equals("ADMIN")) {
            throw new Exception("Cannot rename ADMIN role");
        }
        
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        
        Role updatedRole = roleRepository.save(role);
        log.info("Role updated: {} -> {}", role.getName(), updatedRole.getName());
        
        return convertToResponse(updatedRole);
    }
    
    @Transactional
    public void deleteRole(Long id) throws Exception {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new Exception("Role not found with id: " + id));
        
        // Prevent deletion of critical roles
        if (role.getName().equals("ADMIN")) {
            throw new Exception("Cannot delete ADMIN role as it's required for system operation");
        }
        
        // Check if role has users assigned
        long userCount = userRepository.countByRoleId(id);
        if (userCount > 0) {
            throw new Exception("Cannot delete role because it has " + userCount + " user(s) assigned. Please reassign users first.");
        }
        
        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
    }
    
    @Transactional(readOnly = true)
    public long getUserCountForRole(Long roleId) {
        return userRepository.countByRoleId(roleId);
    }
    
    private RoleResponse convertToResponse(Role role) {
        long userCount = userRepository.countByRoleId(role.getId());
        
        return RoleResponse.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .userCount((int) userCount)
            .build();
    }
}