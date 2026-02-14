package com.ironman.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    // At least one uppercase, one lowercase, one digit, one special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{" + MIN_LENGTH + "," + MAX_LENGTH + "}$"
    );

    /**
     * Validate password strength
     */
    public static boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Get password requirements message
     */
    public static String getRequirementsMessage() {
        return "Password must be " + MIN_LENGTH + "-" + MAX_LENGTH + " characters long and contain at least: " +
                "one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&#)";
    }

    /**
     * Check password strength level
     */
    public static PasswordStrength getStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // Check length
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;

        // Check character types
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[@$!%*?&#].*")) score++;

        if (score <= 3) return PasswordStrength.WEAK;
        if (score <= 5) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }

    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}