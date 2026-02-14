package com.ironman.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersConfig implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // X-Content-Type-Options: Prevent MIME type sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // X-Frame-Options: Prevent clickjacking
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // X-XSS-Protection: Enable XSS protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Strict-Transport-Security: Force HTTPS (enable in production)
        // httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Content-Security-Policy: Prevent XSS and injection attacks
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'"
        );

        // Referrer-Policy: Control referrer information
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions-Policy: Control browser features
        httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=()");

        chain.doFilter(request, response);
    }
}