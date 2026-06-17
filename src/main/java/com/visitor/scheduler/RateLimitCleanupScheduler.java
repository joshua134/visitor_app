package com.visitor.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visitor.service.RateLimiterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitCleanupScheduler {
    
    private final RateLimiterService rateLimiterService;
    
    // Run cleanup every hour
    @Scheduled(fixedRate = 3600000)
    public void cleanupRateLimits() {
        log.debug("Running rate limiter cleanup...");
        rateLimiterService.cleanup();
    }
}