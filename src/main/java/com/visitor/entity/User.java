package com.visitor.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_user", indexes= {
		@Index(name = "idx_email", columnList = "email"),
	    @Index(name = "idx_enabled", columnList = "enabled"),
	    @Index(name = "idx_reset_token", columnList="reset_token")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails  {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String password;
	
	@ManyToOne
	@JoinColumn(name="role_id", nullable=false)
	private Role role;

	private LocalDateTime lastLogin;

	@Column(name = "failed_attempts")
	private int failedAttempts = 0;

	private boolean enabled = false;

	private boolean locked = false;
	
	private boolean expired = false;

	@Column(name = "locked_at")
	private LocalDateTime lockedAt;

	@Column(name = "enabled_at")
	private LocalDateTime enabledAt;

	@Column(nullable = false, unique=true)
	private String email;
	
	@Column(nullable = false)
	private String name;
	
	private String invitationToken;
    private LocalDateTime invitationSentAt;
    private LocalDateTime invitationAcceptedAt;
    
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_sent_at")
    private LocalDateTime resetTokenSentAt;
    
    @Column(name = "reset_token_accepted_at")
    private LocalDateTime resetTokenAcceptedAt;
    
    @Column(name="force_password_change")
    private boolean forcePasswordChange = false;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_"+role.getName()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return !expired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !locked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
