package com.visitor.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.visitor.dto.request.CreateRoleRequest;
import com.visitor.dto.request.UserInvitationRequest;
import com.visitor.dto.response.RoleResponse;
import com.visitor.dto.response.UserResponse;
import com.visitor.dto.response.VisitorResponse;
import com.visitor.entity.User;
import com.visitor.service.ReportService;
import com.visitor.service.RoleService;
import com.visitor.service.SessionService;
import com.visitor.service.UserService;
import com.visitor.service.VisitorService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final VisitorService visitorService;
    private final UserService userService;
    private final RoleService roleService;
    private final ReportService reportService;
    private final SessionService sessionService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
        // Get the user's role
        String role = user.getAuthorities().stream()
            .findFirst()
            .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
            .orElse("VIEWER");
        
       
        
        long totalVisitors = visitorService.getTotalVisitorCount();
        long todayVisitors = visitorService.getTodayVisitorCount();
        long totalUsers = userService.getTotalUserCount();
        long activeSessions = sessionService.getActiveSessions();
        long monthlyVisitors = visitorService.getMonthlyVisitorCount();
        
        List<VisitorResponse> recentVisitors = visitorService.getRecentVisitors();
        
        model.addAttribute("totalVisitors", totalVisitors);
        model.addAttribute("todayVisitors", todayVisitors);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("monthlyVisitors", monthlyVisitors);
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("username", user.getName());
        model.addAttribute("role", role);
        model.addAttribute("currentTime", LocalDateTime.now().format(formatter));
        model.addAttribute("recentVisitors", recentVisitors);
        return "admin/dashboard";
    }
    
    @GetMapping("/visitors")
    public String listVisitors(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "search", required = false) String search,
                               Model model,Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
    	String role = user.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");
            
    	model.addAttribute("username", user.getName());
    	model.addAttribute("role", role);

    	try {
    		PageRequest pageRequest = PageRequest.of(page, size);
    		Page<VisitorResponse> visitorsPage;

    		if (search != null && !search.isEmpty()) {
    			visitorsPage = visitorService.searchVisitors(search, pageRequest);
    			model.addAttribute("searchTerm", search);
    		} else {
    			visitorsPage = visitorService.getAllVisitors(pageRequest);
    		}

    		model.addAttribute("visitors", visitorsPage.getContent());
    		model.addAttribute("currentPage", page);
    		model.addAttribute("totalPages", visitorsPage.getTotalPages());
    		model.addAttribute("totalItems", visitorsPage.getTotalElements());

    		return "admin/visitors-list";
    	} catch (Exception e) {
    		log.error("Failed to load visitors: " + e.getMessage());
    		model.addAttribute("error", "Failed to load visitors: " + e.getMessage());
    		return "admin/visitors-list";
    	}
    }
    
    @GetMapping("/users")
    public String listUsers(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "search", required = false) String search,
                               Model model,Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
    	String role = user.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");
        
        model.addAttribute("username", user.getName());
        model.addAttribute("role", role);
        
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<UserResponse> usersPage;
            
            if (search != null && !search.isEmpty()) {
                usersPage = userService.searchUsers(search, pageRequest);
                model.addAttribute("searchTerm", search);
            } else {
                usersPage = userService.getAllUsers(pageRequest);
            }
            
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalItems", usersPage.getTotalElements());
            
            return "admin/users-list";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load users: " + e.getMessage());
            return "admin/users-list";
        }
    }
    
    @GetMapping("/roles")
    public String listRoles(Model model, Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
    	String role = user.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");
        
        model.addAttribute("username", user.getName());
        model.addAttribute("role", role);
        model.addAttribute("roles", roleService.getAllRoles());
        
        return "admin/roles-list";
    }
    
    @GetMapping("/roles/create")
    public String showCreateRoleForm(Model model,Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
    	String role = user.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");
        
        model.addAttribute("username", user.getName());
        model.addAttribute("role", role);
        model.addAttribute("role", new CreateRoleRequest());
        return "admin/create-role";
    }
    
    @PostMapping("/roles/create")
    public String createRole(@Valid CreateRoleRequest roleRequest,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid role data");
            return "redirect:/admin/roles/create";
        }
        
        try {
            roleService.createRole(roleRequest);
            redirectAttributes.addFlashAttribute("success", "Role created successfully");
            return "redirect:/admin/roles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/roles/create";
        }
    }
    
    @GetMapping("/users/invite")
    public String showInviteUserForm(Model model,Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
        String role = user.getAuthorities().stream()
            .findFirst()
            .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
            .orElse("VIEWER");
        
        model.addAttribute("username", user.getName());
        model.addAttribute("role", role);
        model.addAttribute("invitation", new UserInvitationRequest());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/invite-user";
    }
    
    @PostMapping("/users/invite")
    public String inviteUser(@Valid UserInvitationRequest invitation,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes
                            ,Authentication authentication) {
    	if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user =  (User) authentication.getPrincipal();
        
        if (result.hasErrors()) {
            model.addAttribute("username", user.getName());
            model.addAttribute("role", user.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse("VIEWER"));
            model.addAttribute("roles", roleService.getAllRoles());
            return "admin/invite-user";
        }
        
        try {
            userService.inviteUser(invitation);
            redirectAttributes.addFlashAttribute("success", 
                "Invitation sent successfully to " + invitation.getEmail());
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send invitation: " + e.getMessage());
            return "redirect:/admin/users/invite";
        }
    }
    
    
    @PostMapping("/users/disable/{id}")
    public String disableUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("success", "User disabled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/enable/{id}")
    public String enableUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.enableUser(id);
            redirectAttributes.addFlashAttribute("success", "User enabled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/change-role")
    public String showChangeRoleForm(@PathVariable("id") Long id, 
                                     Model model, 
                                     @AuthenticationPrincipal UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");
        
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("role", role);
        
        try {
            UserResponse user = userService.getUserById(id);
            List<RoleResponse> roles = roleService.getAllRoles();
            
            model.addAttribute("user", user);
            model.addAttribute("roles", roles);
            model.addAttribute("userId", id);
            
            return "admin/change-user-role";
        } catch (Exception e) {
            model.addAttribute("error", "User not found: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/change-role")
    public String updateUserRole(@PathVariable("id") Long id, 
                                 @RequestParam("roleId") Long roleId,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, roleId);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update role: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/roles/{id}/edit")
    public String showEditRoleForm(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("VIEWER");
        
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("role", role);
        
        log.error("role {}",role);
        
        try {
            RoleResponse roleResponse = roleService.getRoleById(id);
            model.addAttribute("roleResponse", roleResponse);
            model.addAttribute("roleId", id);
            return "admin/edit-role";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/roles";
        }
    }
    
    @PostMapping("/roles/{id}/edit")
    public String updateRole(@PathVariable("id") Long id, 
                             @Valid CreateRoleRequest roleRequest,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid role data");
            return "redirect:/admin/roles/" + id + "/edit";
        }
        
        try {
            roleService.updateRole(id, roleRequest);
            redirectAttributes.addFlashAttribute("success", "Role updated successfully");
            return "redirect:/admin/roles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/roles/" + id + "/edit";
        }
    }
    
    @PostMapping("/roles/{id}/delete")
    public String deleteRole(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Role deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
    
    
    @GetMapping("/reports/export/excel")
    public void exportToExcel(@RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             HttpServletResponse response) throws Exception {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=visitors_report.xlsx");
        
        List<VisitorResponse> visitors;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            visitors = visitorService.getVisitorsByDateRange(start, end);
        } else {
            visitors = visitorService.getAllVisitorsUnpaged();
        }
        
        reportService.exportToExcel(response, visitors);
    }

    @GetMapping("/reports/export/pdf")
    public void exportToPDF(@RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           HttpServletResponse response) throws Exception {
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=visitors_report.pdf");
        
        List<VisitorResponse> visitors;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            visitors = visitorService.getVisitorsByDateRange(start, end);
        } else {
            visitors = visitorService.getAllVisitorsUnpaged();
        }
        
        reportService.exportToPDF(response, visitors);
    }

    @GetMapping("/reports/print")
    public String printReport(Model model, @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        
        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
            .orElse("VIEWER");
        
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("role", role);
        
        List<VisitorResponse> visitors;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            visitors = visitorService.getVisitorsByDateRange(start, end);
            model.addAttribute("dateRange", startDate + " to " + endDate);
        } else {
            visitors = visitorService.getAllVisitorsUnpaged();
            model.addAttribute("dateRange", "All Time");
        }
        
        model.addAttribute("visitors", visitors);
        model.addAttribute("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("totalVisitors", visitors.size());
        
        return "admin/print-view";
    }
}
