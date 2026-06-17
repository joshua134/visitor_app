package com.visitor.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visitor.dto.request.VisitorRegisterRequest;
import com.visitor.dto.response.VisitorResponse;
import com.visitor.entity.Visitor;
import com.visitor.repository.VisitorRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VisitorService {
	private final VisitorRepository visitorRepository;
	private final EmailService emailService;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	@Transactional
    public VisitorResponse registerVisitor(VisitorRegisterRequest dto, HttpServletRequest request) throws Exception {
        log.info("Registering new visitor with email: {}", dto.getEmail());
        
        // Check for duplicates
//        Visitor existingVisitor = visitorRepository.findByEmail(dto.getEmail());
//        if (existingVisitor != null) {
//            throw new Exception("Visitor with this email already exists");
//        }
        
        Visitor visitor = new Visitor();
        visitor.setFirstName(dto.getFirstName());
        visitor.setLastName(dto.getLastName());
        visitor.setDateOfBirth(dto.getDateOfBirth());
        visitor.setNationalId(dto.getNationalId());
        visitor.setGender(dto.getGender());
        visitor.setPhoneNumber(dto.getPhoneNumber());
        visitor.setEmail(dto.getEmail());
//        visitor.setAdditionalDetails(dto.getAdditionalDetails());
        visitor.setIpAddress(getClientIp(request));
        visitor.setUserAgent(request.getHeader("User-Agent"));
        
        Visitor savedVisitor = visitorRepository.save(visitor);
        
        // Send confirmation email
        try {
            emailService.sendRegistrationConfirmation(dto.getEmail(), dto.getFirstName(), dto.getLastName());        	
        }catch(Exception e) {
        	log.error(e.getMessage()," -> {} ", e);
        }

        
        log.info("Visitor registered successfully with ID: {}", savedVisitor.getId());
        return convertToResponse(savedVisitor);
    }
    
    @Transactional(readOnly = true)
    public Page<VisitorResponse> getAllVisitors(Pageable pageable) {
    	Page<Visitor> visitorsPage = visitorRepository.findAll(pageable);
        return visitorsPage.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<VisitorResponse> searchVisitors(String searchTerm, Pageable pageable) {
    	Page<Visitor> visitorsPage = visitorRepository.searchVisitors(searchTerm, pageable);
        return visitorsPage.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public VisitorResponse getVisitorById(Long id) throws Exception {
    	Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new Exception("Visitor not found with id: " + id));
            return convertToResponse(visitor);
    }
    
    @Transactional(readOnly = true)
    public List<VisitorResponse> getRecentVisitors() {
        Page<Visitor> visitorsPage = visitorRepository.findAll(PageRequest.of(0, 15));
        return visitorsPage.map(this::convertToResponse).getContent();
    }
    
    @Transactional(readOnly = true)
    public long getTotalVisitorCount() {
        return visitorRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long getTodayVisitorCount() {
        return visitorRepository.countByCreatedAtBetween(
            LocalDate.now().atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay()
        );
    }
    
    private VisitorResponse convertToResponse(Visitor visitor) {
        int age = Period.between(visitor.getDateOfBirth(), LocalDate.now()).getYears();
        
        return VisitorResponse.builder()
            .id(visitor.getId())
            .firstName(visitor.getFirstName())
            .lastName(visitor.getLastName())
            .fullName(visitor.getFirstName() + " " + visitor.getLastName())
            .dateOfBirth(visitor.getDateOfBirth())
            .age(age)
            .nationalId(visitor.getNationalId())
            .gender(visitor.getGender())
            .phoneNumber(visitor.getPhoneNumber())
            .email(visitor.getEmail())
            .ipAddress(visitor.getIpAddress())
            .registrationDate(visitor.getCreatedAt().format(formatter))
            .build();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @Transactional(readOnly = true)
    public List<VisitorResponse> getAllVisitorsUnpaged() {
        List<Visitor> visitors = visitorRepository.findAll();
        return visitors.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<VisitorResponse> getVisitorsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<Visitor> visitors = visitorRepository.findByCreatedAtBetween(start, end);
        return visitors.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long getMonthlyVisitorCount() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
        return visitorRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
    }
	
}
