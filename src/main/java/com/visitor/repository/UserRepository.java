package com.visitor.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.visitor.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String username);

	Optional<User> findByInvitationToken(String token);
	
	Optional<User> findByResetToken(String token);
	
	@Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId")
    long countByRoleId(@Param("roleId") Long roleId);

	boolean existsByEmail(String email);
	
	Page<User> findByEnabled(boolean enabled, Pageable pageable);
    
	@Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
			"LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))")
	Page<User> searchUsers(@Param("search") String search, Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(@Param("email") String email);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedAttempts = 0, u.locked = false, u.lockedAt = null WHERE u.email = :email")
    void resetFailedAttempts(@Param("email") String email);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.locked = true, u.lockedAt = :lockTime WHERE u.email = :email")
    void lockUser(@Param("email") String email, @Param("lockTime") LocalDateTime lockTime);

}
