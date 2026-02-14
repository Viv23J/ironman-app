package com.ironman.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyValidator {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate secure API key
     */
    public static String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Validate API key format
     */
    public static boolean isValidFormat(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        // Check if it's a valid Base64 URL-safe string
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(apiKey);
            return decoded.length == 32;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}