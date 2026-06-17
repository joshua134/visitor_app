package com.visitor.service;

import com.visitor.dto.request.VisitorRegisterRequest;
import com.visitor.dto.response.VisitorResponse;
import com.visitor.entity.Visitor;
import com.visitor.repository.VisitorRepository;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDataService {
    
    private final VisitorService visitorService;
    private final VisitorRepository visitorRepository;
    private final Random random = new Random();
    
    // Sample names
    private final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Lisa", "Robert", "Maria",
        "William", "Patricia", "Richard", "Jennifer", "Thomas", "Linda", "Charles", "Elizabeth",
        "Christopher", "Susan", "Daniel", "Jessica", "Matthew", "Karen", "Anthony", "Nancy",
        "Donald", "Betty", "Mark", "Helen", "Paul", "Sandra", "Steven", "Donna", "Andrew", "Carol",
        "Kenneth", "Ruth", "Joshua", "Sharon", "Kevin", "Michelle", "Brian", "Laura", "George", "Amanda",
        "Edward", "Deborah", "Ronald", "Stephanie", "Timothy", "Rebecca", "Jason", "Laura", "Jeffrey", "Amy"
    };
    
    private final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee", "White", "Harris",
        "Clark", "Lewis", "Robinson", "Walker", "Young", "Hall", "Allen", "King", "Wright", "Scott",
        "Green", "Baker", "Adams", "Nelson", "Hill", "Ramirez", "Campbell", "Mitchell", "Roberts", "Carter",
        "Phillips", "Evans", "Turner", "Torres", "Parker", "Collins", "Edwards", "Stewart", "Flores", "Morris"
    };
    
    private final String[] DOMAINS = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "company.com", "business.com", "example.com"};
    private final String[] GENDERS = {"MALE", "FEMALE", "OTHER"};
    private final String[] COUNTRIES = {"USA", "UK", "Canada", "Australia", "Germany", "France", "Japan", "Brazil", "India", "South Africa"};
    
    /**
     * Generate test visitors with random dates within a range
     */
    @Transactional
    public TestDataResult generateTestVisitors(int count, LocalDate startDate, LocalDate endDate) {
        log.info("Starting to generate {} test visitors from {} to {}", count, startDate, endDate);
        
        List<VisitorResponse> createdVisitors = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        long dateRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        for (int i = 0; i < count; i++) {
            try {
                // Generate random registration date within range
                LocalDateTime registrationDate = startDate.atStartOfDay()
                    .plusDays(random.nextInt((int) dateRange + 1))
                    .plusHours(random.nextInt(24))
                    .plusMinutes(random.nextInt(60))
                    .plusSeconds(random.nextInt(60));
                
                // Create visitor request
                VisitorRegisterRequest request = createRandomVisitorRequest(i);
                
                // Create mock request
                MockHttpServletRequest mockRequest = new MockHttpServletRequest();
                mockRequest.setRemoteAddr("127.0.0.1");
                mockRequest.setUserAgent("TestDataGenerator/1.0");
                
                // Save via service
                VisitorResponse response = visitorService.registerVisitor(request, mockRequest);
                
                // Update the creation date in database directly (bypass encryption for date only)
                updateVisitorCreatedDate(response.getId(), registrationDate);
                
                createdVisitors.add(response);
                successCount.incrementAndGet();
                
                if ((i + 1) % 50 == 0) {
                    log.info("Generated {} of {} visitors", i + 1, count);
                }
                
            } catch (Exception e) {
                log.error("Failed to generate visitor {}: {}", i, e.getMessage());
                errors.add("Failed to generate visitor " + i + ": " + e.getMessage());
                failCount.incrementAndGet();
            }
        }
        
        log.info("Completed generating test visitors. Success: {}, Failed: {}", successCount.get(), failCount.get());
        
        return TestDataResult.builder()
            .totalRequested(count)
            .successCount(successCount.get())
            .failCount(failCount.get())
            .visitors(createdVisitors)
            .errors(errors)
            .build();
    }
    
    /**
     * Generate visitors with specific date patterns (e.g., evenly distributed or clustered)
     */
    @Transactional
    public TestDataResult generateVisitorsWithPattern(int count, DatePattern pattern, LocalDate startDate, LocalDate endDate) {
        log.info("Generating {} visitors with pattern: {}", count, pattern);
        
        List<VisitorResponse> createdVisitors = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        
        long dateRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        for (int i = 0; i < count; i++) {
            try {
                LocalDateTime registrationDate;
                
                switch (pattern) {
                    case EVENLY_DISTRIBUTED:
                        // Evenly distribute across date range
                        long daysOffset = (i * dateRange) / count;
                        registrationDate = startDate.atStartOfDay().plusDays(daysOffset);
                        break;
                        
                    case RECENT_FIRST:
                        // More recent dates first
                        double ratio = 1.0 - ((double) i / count);
                        long recentDays = (long) (dateRange * ratio);
                        registrationDate = endDate.atStartOfDay().minusDays(recentDays);
                        break;
                        
                    case CLUSTERED:
                        // Cluster around 3 specific dates
                        int clusterPoint = i % 3;
                        long clusterOffset = clusterPoint * (dateRange / 3);
                        registrationDate = startDate.atStartOfDay().plusDays(clusterOffset + random.nextInt(7));
                        break;
                        
                    case WEEKEND_HEAVY:
                        // More visitors on weekends
                        LocalDate date = startDate.plusDays(random.nextInt((int) dateRange + 1));
                        while (date.getDayOfWeek().getValue() < 6 && random.nextInt(100) < 70) {
                            date = startDate.plusDays(random.nextInt((int) dateRange + 1));
                        }
                        registrationDate = date.atStartOfDay().plusHours(random.nextInt(24));
                        break;
                        
                    default:
                        registrationDate = startDate.atStartOfDay()
                            .plusDays(random.nextInt((int) dateRange + 1))
                            .plusHours(random.nextInt(24));
                }
                
                VisitorRegisterRequest request = createRandomVisitorRequest(i);
                MockHttpServletRequest mockRequest = new MockHttpServletRequest();
                mockRequest.setRemoteAddr("127.0.0.1");
                
                VisitorResponse response = visitorService.registerVisitor(request, mockRequest);
                updateVisitorCreatedDate(response.getId(), registrationDate);
                
                createdVisitors.add(response);
                successCount.incrementAndGet();
                
            } catch (Exception e) {
                log.error("Failed to generate visitor: {}", e.getMessage());
            }
        }
        
        return TestDataResult.builder()
            .totalRequested(count)
            .successCount(successCount.get())
            .failCount(count - successCount.get())
            .visitors(createdVisitors)
            .build();
    }
    
    private VisitorRegisterRequest createRandomVisitorRequest(int index) {
        VisitorRegisterRequest request = new VisitorRegisterRequest();
        
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setDateOfBirth(generateRandomBirthDate());
        request.setNationalId(generateUniqueNationalId(index));
        request.setGender(GENDERS[random.nextInt(GENDERS.length)]);
        request.setPhoneNumber(generateUniquePhoneNumber(index));
        request.setEmail(generateUniqueEmail(firstName, lastName, index));
        request.setCaptcha("test123"); // This will be validated by controller
        
        return request;
    }
    
    private LocalDate generateRandomBirthDate() {
        int year = 1950 + random.nextInt(50);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return LocalDate.of(year, month, day);
    }
    
    private String generateUniqueNationalId(int index) {
        return "ID" + System.currentTimeMillis() + index + random.nextInt(10000);
    }
    
    private String generateUniquePhoneNumber(int index) {
        return "+1" + (1000000000L + index + random.nextInt(900000000));
    }
    
    private String generateUniqueEmail(String firstName, String lastName, int index) {
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + index + "@" + domain;
        return email.replaceAll("\\s", "");
    }
    
    private void updateVisitorCreatedDate(Long visitorId, LocalDateTime newDate) {
        visitorRepository.findById(visitorId).ifPresent(visitor -> {
            try {
                java.lang.reflect.Field field = Visitor.class.getDeclaredField("createdAt");
                field.setAccessible(true);
                field.set(visitor, newDate);
                visitorRepository.save(visitor);
            } catch (Exception e) {
                log.error("Failed to update created date for visitor {}: {}", visitorId, e.getMessage());
            }
        });
    }
    
 // Mock HttpServletRequest implementation
    private static class MockHttpServletRequest implements HttpServletRequest {
        private String remoteAddr = "127.0.0.1";
        private String userAgent = "TestDataGenerator/1.0";
        private Map<String, String[]> parameters = new HashMap<>();
        private Map<String, Object> attributes = new HashMap<>();
        private String method = "POST";
        private String requestURI = "/register";
        
        public void setRemoteAddr(String remoteAddr) { this.remoteAddr = remoteAddr; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public void setParameter(String name, String value) { 
            this.parameters.put(name, new String[]{value}); 
        }
        public void setMethod(String method) { this.method = method; }
        
        @Override 
        public String getRemoteAddr() { return remoteAddr; }
        
        @Override 
        public String getHeader(String name) { 
            if ("User-Agent".equalsIgnoreCase(name)) return userAgent;
            if ("X-Forwarded-For".equalsIgnoreCase(name)) return null;
            return null;
        }
        
        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }
        
        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(attributes.keySet());
        }
        
        @Override
        public String getCharacterEncoding() {
            return "UTF-8";
        }
        
        @Override
        public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
            // Do nothing
        }
        
        @Override
        public int getContentLength() {
            return 0;
        }
        
        @Override
        public long getContentLengthLong() {
            return 0;
        }
        
        @Override
        public String getContentType() {
            return "application/x-www-form-urlencoded";
        }
        
        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
                
                @Override
                public boolean isFinished() {
                    return true;
                }
                
                @Override
                public boolean isReady() {
                    return true;
                }
                
                @Override
                public void setReadListener(jakarta.servlet.ReadListener listener) {
                    // Do nothing
                }
            };
        }
        
        @Override
        public String getParameter(String name) {
            String[] values = parameters.get(name);
            return values != null && values.length > 0 ? values[0] : null;
        }
        
        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
        }
        
        @Override
        public String[] getParameterValues(String name) {
            return parameters.get(name);
        }
        
        @Override
        public Map<String, String[]> getParameterMap() {
            return parameters;
        }
        
        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }
        
        @Override
        public String getScheme() {
            return "http";
        }
        
        @Override
        public String getServerName() {
            return "localhost";
        }
        
        @Override
        public int getServerPort() {
            return 8080;
        }
        
        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new StringReader(""));
        }
        
        @Override
        public String getRemoteHost() {
            return "localhost";
        }
        
        @Override
        public void setAttribute(String name, Object o) {
            attributes.put(name, o);
        }
        
        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }
        
        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
        
        @Override
        public Enumeration<Locale> getLocales() {
            return Collections.enumeration(Collections.singletonList(Locale.getDefault()));
        }
        
        @Override
        public boolean isSecure() {
            return false;
        }
        
        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }
        
        @Override
        public int getRemotePort() {
            return 54321;
        }
        
        @Override
        public String getLocalName() {
            return "localhost";
        }
        
        @Override
        public String getLocalAddr() {
            return "127.0.0.1";
        }
        
        @Override
        public int getLocalPort() {
            return 8080;
        }
        
        @Override
        public ServletContext getServletContext() {
            return null;
        }
        
        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return null;
        }
        
        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IllegalStateException {
            return null;
        }
        
        @Override
        public boolean isAsyncStarted() {
            return false;
        }
        
        @Override
        public boolean isAsyncSupported() {
            return false;
        }
        
        @Override
        public AsyncContext getAsyncContext() {
            return null;
        }
        
        @Override
        public DispatcherType getDispatcherType() {
            return DispatcherType.REQUEST;
        }
        
        @Override
        public String getRequestId() {
            return UUID.randomUUID().toString();
        }
        
        @Override
        public String getProtocolRequestId() {
            return UUID.randomUUID().toString();
        }
        
        @Override
        public ServletConnection getServletConnection() {
            return null;
        }
        
        @Override
        public String getAuthType() {
            return null;
        }
        
        @Override
        public jakarta.servlet.http.Cookie[] getCookies() {
            return new jakarta.servlet.http.Cookie[0];
        }
        
        @Override
        public long getDateHeader(String name) {
            return -1;
        }
        
        @Override
        public Enumeration<String> getHeaders(String name) {
            return Collections.emptyEnumeration();
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.emptyEnumeration();
        }
        
        @Override
        public int getIntHeader(String name) {
            return -1;
        }
        
        @Override
        public String getMethod() {
            return method;
        }
        
        @Override
        public String getPathInfo() {
            return null;
        }
        
        @Override
        public String getPathTranslated() {
            return null;
        }
        
        @Override
        public String getContextPath() {
            return "";
        }
        
        @Override
        public String getQueryString() {
            return null;
        }
        
        @Override
        public String getRemoteUser() {
            return null;
        }
        
        @Override
        public boolean isUserInRole(String role) {
            return false;
        }
        
        @Override
        public java.security.Principal getUserPrincipal() {
            return null;
        }
        
        @Override
        public String getRequestedSessionId() {
            return null;
        }
        
        @Override
        public String getRequestURI() {
            return requestURI;
        }
        
        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer("http://localhost:8080" + requestURI);
        }
        
        @Override
        public String getServletPath() {
            return "";
        }
        
        @Override
        public jakarta.servlet.http.HttpSession getSession(boolean create) {
            return null;
        }
        
        @Override
        public jakarta.servlet.http.HttpSession getSession() {
            return null;
        }
        
        @Override
        public String changeSessionId() {
            return UUID.randomUUID().toString();
        }
        
        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }
        
        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }
        
        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }
        
        @Override
        public boolean authenticate(jakarta.servlet.http.HttpServletResponse response) throws IOException, ServletException {
            return false;
        }
        
        @Override
        public void login(String username, String password) throws ServletException {
            // Do nothing
        }
        
        @Override
        public void logout() throws ServletException {
            // Do nothing
        }
        
        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return Collections.emptyList();
        }
        
        @Override
        public Part getPart(String name) throws IOException, ServletException {
            return null;
        }
        
        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass)
                throws IOException, ServletException {
            return null;
        }
    }
    
    // Result class
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestDataResult {
        private int totalRequested;
        private int successCount;
        private int failCount;
        private List<VisitorResponse> visitors;
        private List<String> errors;
    }
    
    // Date pattern enum
    public enum DatePattern {
        RANDOM,           // Completely random dates
        EVENLY_DISTRIBUTED, // Evenly spread across date range
        RECENT_FIRST,     // More recent dates
        CLUSTERED,        // Clustered around specific dates
        WEEKEND_HEAVY     // More visitors on weekends
    }
}