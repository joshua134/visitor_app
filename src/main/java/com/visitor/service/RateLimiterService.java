package com.visitor.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimiterService {

    // ===== MORE TOLERANT LIMITS =====

    private static final int PUBLIC_ENDPOINT_LIMIT = 50;     
    private static final int PUBLIC_ENDPOINT_WINDOW = 5;

    private static final int LOGIN_LIMIT = 10;               
    private static final int LOGIN_WINDOW = 10;

    private static final int AUTH_ENDPOINT_LIMIT = 250;      
    private static final int AUTH_ENDPOINT_WINDOW = 1;

    private static final int REGISTRATION_LIMIT = 10;        
    private static final int REGISTRATION_WINDOW = 30;      

    // BURST tolerance (NEW)
    private static final int BURST_ALLOWANCE = 10;

    // ===== STORAGE =====
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final Map<String, BlockInfo> blockedMap = new ConcurrentHashMap<>();

    // ===== TYPES =====
    public enum EndpointType {
        PUBLIC,
        LOGIN,
        REGISTRATION,
        FORGOT_PASSWORD,
        RESET_PASSWORD,
        AUTHENTICATED
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RateLimitInfo {
        private int count;
        private long windowStart;
        private long lastAttempt;
        private int burst; // NEW
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BlockInfo {
        private long blockedUntil;
        private String reason;
        private int attempts;
    }

    // ===== MAIN CHECK =====
    public boolean isAllowed(String key, EndpointType endpointType) {

        if (isBlocked(key)) {
            return false;
        }

        int limit = getLimit(endpointType);
        int windowMinutes = getWindow(endpointType);
        long windowMillis = windowMinutes * 60 * 1000L;

        RateLimitInfo info = rateLimitMap.compute(key, (k, v) -> {
            long now = System.currentTimeMillis();

            // first request
            if (v == null) {
                return new RateLimitInfo(1, now, now, 0);
            }

            // reset window if expired
            if (now - v.getWindowStart() > windowMillis) {
                return new RateLimitInfo(1, now, now, 0);
            }

            // BURST handling (NEW)
            if (now - v.getLastAttempt() < 3000) { // 3 seconds burst window
                v.setBurst(v.getBurst() + 1);
            } else {
                v.setBurst(Math.max(0, v.getBurst() - 1));
            }

            v.setCount(v.getCount() + 1);
            v.setLastAttempt(now);

            return v;
        });

        int effectiveLimit = limit + BURST_ALLOWANCE;

        // only block if REALLY exceeded
        if (info.getCount() > effectiveLimit) {

            // ⚠️ IMPORTANT CHANGE: soft block instead of instant punishment
            if (info.getCount() > effectiveLimit + 10) {
                block(key, 10, "Heavy abuse detected");
            }

            return false;
        }

        return true;
    }

    // ===== BLOCK CHECK =====
    public boolean isBlocked(String key) {
        BlockInfo blockInfo = blockedMap.get(key);

        if (blockInfo == null) return false;

        long now = System.currentTimeMillis();

        if (now > blockInfo.getBlockedUntil()) {
            blockedMap.remove(key);
            return false;
        }

        return true;
    }

    // ===== BLOCK =====
    public void block(String key, int durationMinutes, String reason) {
        long blockedUntil = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        blockedMap.put(key, new BlockInfo(blockedUntil, reason, 0));
    }
    
   

    // ===== FAILURE HANDLING (SOFTER) =====
    public void registerFailure(String key, int maxFailures) {
        BlockInfo blockInfo = blockedMap.compute(key, (k, v) -> {
            if (v == null) {
                return new BlockInfo(0, "fail", 1);
            }
            v.setAttempts(v.getAttempts() + 1);
            return v;
        });

        if (blockInfo.getAttempts() >= maxFailures) {
            block(key, 30, "Too many failed attempts");
        }
    }

    public void resetFailures(String key) {
        blockedMap.remove(key);
        rateLimitMap.remove(key);
    }

    // ===== CONFIG =====
    private int getLimit(EndpointType type) {
        return switch (type) {
            case LOGIN -> LOGIN_LIMIT;
            case REGISTRATION -> REGISTRATION_LIMIT;
            case FORGOT_PASSWORD, RESET_PASSWORD -> 5;
            case AUTHENTICATED -> AUTH_ENDPOINT_LIMIT;
            default -> PUBLIC_ENDPOINT_LIMIT;
        };
    }

    private int getWindow(EndpointType type) {
        return switch (type) {
            case LOGIN -> LOGIN_WINDOW;
            case REGISTRATION -> REGISTRATION_WINDOW;
            case FORGOT_PASSWORD, RESET_PASSWORD -> 30;
            case AUTHENTICATED -> AUTH_ENDPOINT_WINDOW;
            default -> PUBLIC_ENDPOINT_WINDOW;
        };
    }

    // ===== STATUS =====
    public RateLimitStatus getStatus(String key, EndpointType type) {
        RateLimitInfo info = rateLimitMap.get(key);

        if (info == null) {
            return new RateLimitStatus(0, getLimit(type), getWindow(type), false, false, 0);
        }

        return new RateLimitStatus(
                info.getCount(),
                getLimit(type),
                getWindow(type),
                false,
                isBlocked(key),
                0
        );
    }

    // ===== CLEANUP =====
    public void cleanup() {
        long now = System.currentTimeMillis();
        long maxAge = 24 * 60 * 60 * 1000L;

        rateLimitMap.entrySet().removeIf(e ->
                now - e.getValue().getLastAttempt() > maxAge
        );

        blockedMap.entrySet().removeIf(e ->
                now > e.getValue().getBlockedUntil()
        );
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RateLimitStatus {
        private int currentCount;
        private int limit;
        private int windowMinutes;
        private boolean windowExpired;
        private boolean blocked;
        private long blockedUntil;
    }
}
