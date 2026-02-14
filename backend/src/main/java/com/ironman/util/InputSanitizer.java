package com.ironman.util;

import org.apache.commons.text.StringEscapeUtils;

public class InputSanitizer {

    /**
     * Sanitize user input to prevent XSS
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Trim whitespace
        input = input.trim();

        // HTML escape
        input = StringEscapeUtils.escapeHtml4(input);

        // Remove null bytes
        input = input.replace("\0", "");

        return input;
    }

    /**
     * Sanitize email
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }

        email = email.trim().toLowerCase();

        // Basic email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return null;
        }

        return email;
    }

    /**
     * Sanitize phone number
     */
    public static String sanitizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        // Remove all non-digit characters except +
        phone = phone.replaceAll("[^0-9+]", "");

        return phone;
    }

    /**
     * Validate and sanitize file path
     */
    public static boolean isValidFilePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // Prevent directory traversal
        if (path.contains("..") || path.contains("./") || path.contains(":\\")) {
            return false;
        }

        return true;
    }
}