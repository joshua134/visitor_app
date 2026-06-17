package com.visitor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.visitor.entity.Visitor;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
	Page<Visitor> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

	List<Visitor> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	
	@Query("SELECT v FROM Visitor v WHERE " +
			"LOWER(v.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
			"LOWER(v.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
			"LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
			"LOWER(v.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
	Page<Visitor> searchVisitors(@Param("search") String search, Pageable pageable);

	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	@Query("SELECT FUNCTION('DATE', v.createdAt) as date, COUNT(v) as count FROM Visitor v " +
			"WHERE v.createdAt BETWEEN :start AND :end GROUP BY FUNCTION('DATE', v.createdAt)")
	List<Object[]> getDailyVisitorStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
	
	Optional<Visitor> findByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByNationalId(String nationalId);
}
