package com.ironman.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityAuditLogger {

    /**
     * Log authentication attempt
     */
    public void logAuthAttempt(String phone, boolean success) {
        if (success) {
            log.info("SECURITY AUDIT - Login SUCCESS: phone={}", phone);
        } else {
            log.warn("SECURITY AUDIT - Login FAILED: phone={}", phone);
        }
    }

    /**
     * Log failed authentication attempts (potential brute force)
     */
    public void logSuspiciousActivity(String phone, String ip) {
        log.warn("SECURITY AUDIT - SUSPICIOUS ACTIVITY: phone={}, ip={}", phone, ip);
    }

    /**
     * Log unauthorized access attempt
     */
    public void logUnauthorizedAccess(String endpoint, String ip) {
        log.warn("SECURITY AUDIT - UNAUTHORIZED ACCESS: endpoint={}, ip={}", endpoint, ip);
    }

    /**
     * Log data modification
     */
    public void logDataModification(String userId, String action, String entity) {
        log.info("SECURITY AUDIT - DATA MODIFICATION: userId={}, action={}, entity={}",
                userId, action, entity);
    }

    /**
     * Log rate limit violation
     */
    public void logRateLimitViolation(String ip) {
        log.warn("SECURITY AUDIT - RATE LIMIT VIOLATION: ip={}", ip);
    }
}