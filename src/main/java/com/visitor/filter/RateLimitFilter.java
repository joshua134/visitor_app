package com.visitor.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.visitor.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Component
//@Order(1)
//@RequiredArgsConstructor
//@Slf4j
//public class RateLimitFilter extends OncePerRequestFilter {
//	private final RateLimiterService rateLimiterService;
//	
//	private static final List<String> PUBLIC_ENDPOINTS = List.of(
//	        "/", "/register", "/captcha-image", "/refresh-captcha"
//	    );
//	
//	// Login endpoint
//    private static final String LOGIN_ENDPOINT = "/login";
//    // Forgot password endpoint
//    private static final String FORGOT_PASSWORD = "/forgot-password";
//    // Reset password endpoint
//    private static final String RESET_PASSWORD = "/reset-password";
//    // Admin endpoints (authenticated)
//    private static final String ADMIN_PREFIX = "/admin";
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//		String clientIp = getClientIp(request);
//        String requestUri = request.getRequestURI();
//        String method = request.getMethod();
//        
//        log.info("method =? {}", method);
//        
//        // Allow normal page navigation (VERY IMPORTANT FIX)
//        if ("GET".equals(method) && isPageNavigation(requestUri)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        
//        // Skip rate limiting for static resources
//        if (isStaticResource(requestUri)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        
//        // Determine endpoint type
//        RateLimiterService.EndpointType endpointType = determineEndpointType(requestUri, method);
//        
//        // Generate key for rate limiting
//        String key = generateKey(clientIp, endpointType, requestUri);
//
//        if (!rateLimiterService.isAllowed(key, endpointType)) {
//            log.warn("Rate limit exceeded for IP: {}, URI: {}, Method: {}", clientIp, requestUri, method);
//            
//            response.setStatus(429);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\",\"message\":\"Rate limit exceeded. Please wait before making more requests.\"}");
//            response.getWriter().flush();
//            return;
//        }
//        
//        // Check if IP is blocked (for login failures)
//        if (rateLimiterService.isBlocked(clientIp)) {
//            log.warn("Request from blocked IP: {}", clientIp);
//            response.setStatus(429);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\":\"Too many failed attempts. Please try again later.\",\"message\":\"Your IP has been temporarily blocked due to multiple failed attempts.\"}");
//            response.getWriter().flush();
//            return;
//        }
//        
//        // Proceed with the request
//        filterChain.doFilter(request, response);
//        
//        // Track successful actions (reset failure count)
//        if (isSuccessfulAction(requestUri, method, response.getStatus())) {
//            rateLimiterService.resetFailures(clientIp);
//        }
//	}
//
//	private RateLimiterService.EndpointType determineEndpointType(String uri, String method) {
//        // Login endpoint - stricter limits
//        if (uri.equals(LOGIN_ENDPOINT) && method.equals("POST")) {
//            return RateLimiterService.EndpointType.LOGIN;
//        }
//        
//        // Registration endpoint
//        if (uri.equals("/register") && method.equals("POST")) {
//            return RateLimiterService.EndpointType.REGISTRATION;
//        }
//        
//        // Forgot password endpoints
//        if (uri.equals(FORGOT_PASSWORD) || uri.startsWith(FORGOT_PASSWORD)) {
//            return RateLimiterService.EndpointType.FORGOT_PASSWORD;
//        }
//        
//        // Reset password endpoints
//        if (uri.equals(RESET_PASSWORD) || uri.startsWith(RESET_PASSWORD)) {
//            return RateLimiterService.EndpointType.RESET_PASSWORD;
//        }
//        
//        // Admin authenticated endpoints
//        if (uri.startsWith(ADMIN_PREFIX)) {
//            return RateLimiterService.EndpointType.AUTHENTICATED;
//        }
//        
//        // Public endpoints
//        return RateLimiterService.EndpointType.PUBLIC;
//    }
//    
//    private String generateKey(String clientIp, RateLimiterService.EndpointType endpointType, String uri) {
//        switch (endpointType) {
//            case LOGIN:
//                return clientIp + ":login";
//            case REGISTRATION:
//                return clientIp + ":registration";
//            case FORGOT_PASSWORD:
//                return clientIp + ":forgot-password";
//            case RESET_PASSWORD:
//                return clientIp + ":reset-password";
//            case AUTHENTICATED:
//                // For authenticated endpoints, use a session or user-based key
//                return clientIp + ":admin";
//            default:
//                return clientIp + ":public";
//        }
//    }
//    
//    private String getClientIp(HttpServletRequest request) {
//        String xfHeader = request.getHeader("X-Forwarded-For");
//        if (xfHeader != null && !xfHeader.isEmpty()) {
//            return xfHeader.split(",")[0];
//        }
//        return request.getRemoteAddr();
//    }
//    
//    private boolean isPageNavigation(String uri) {
//        return uri.equals("/") ||
//               uri.startsWith("/dashboard") ||
//               uri.startsWith("/login") ||
//               uri.startsWith("/register") ||
//               uri.startsWith("/admin") ||
//               uri.startsWith("/forgot-password") ||
//               uri.startsWith("/reset-password") ||
//               uri.endsWith(".html");
//    }
//    
//    private boolean isStaticResource(String uri) {
//        return uri.startsWith("/css/") || 
//               uri.startsWith("/js/") || 
//               uri.startsWith("/images/") || 
//               uri.startsWith("/webjars/") ||
//               uri.startsWith("/favicon.ico");
//    }
//    
//    private boolean isSuccessfulAction(String uri, String method, int status) {
//        // Successful login
//        if (uri.equals(LOGIN_ENDPOINT) && method.equals("POST") && status == 302) {
//            return true;
//        }
//        // Successful registration
//        if (uri.equals("/register") && method.equals("POST") && status == 302) {
//            return true;
//        }
//        return false;
//    }
//}
