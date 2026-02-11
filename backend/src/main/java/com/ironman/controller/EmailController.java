package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    /**
     * Test simple email
     */
    @PostMapping("/test/simple")
    public ResponseEntity<ApiResponse<String>> sendTestEmail(
            @RequestParam String toEmail) {

        log.info("Sending test email to: {}", toEmail);

        emailService.sendSimpleEmail(
                toEmail,
                "Test Email from IronMan Laundry",
                "Hello! This is a test email from IronMan Laundry Service. If you received this, email configuration is working correctly!"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Test email sent successfully to " + toEmail, null));
    }

    /**
     * Test order confirmation email
     */
    @PostMapping("/test/order-confirmation")
    public ResponseEntity<ApiResponse<String>> sendOrderConfirmationEmail(
            @RequestParam String toEmail) {

        log.info("Sending order confirmation email to: {}", toEmail);

        emailService.sendOrderConfirmationEmail(
                toEmail,
                "John Doe",
                "IM-2026-TEST-001",
                new BigDecimal("150.00"),
                "15 Feb 2026"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Order confirmation email sent to " + toEmail, null));
    }

    /**
     * Test payment success email
     */
    @PostMapping("/test/payment-success")
    public ResponseEntity<ApiResponse<String>> sendPaymentSuccessEmail(
            @RequestParam String toEmail) {

        log.info("Sending payment success email to: {}", toEmail);

        emailService.sendPaymentSuccessEmail(
                toEmail,
                "John Doe",
                "IM-2026-TEST-001",
                new BigDecimal("150.00")
        );

        return ResponseEntity.ok(
                ApiResponse.success("Payment success email sent to " + toEmail, null));
    }

    /**
     * Test OTP email
     */
    @PostMapping("/test/otp")
    public ResponseEntity<ApiResponse<String>> sendOtpEmail(
            @RequestParam String toEmail) {

        log.info("Sending OTP email to: {}", toEmail);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.sendOtpEmail(toEmail, otp);

        return ResponseEntity.ok(
                ApiResponse.success("OTP email sent to " + toEmail + ". OTP: " + otp, null));
    }
}