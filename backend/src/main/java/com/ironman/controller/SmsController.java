package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.SmsResponse;
import com.ironman.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final SmsService smsService;

    /**
     * Test SMS - Simple text
     */
    @PostMapping("/test/simple")
    public ResponseEntity<ApiResponse<SmsResponse>> sendTestSms(
            @RequestParam String phoneNumber) {

        log.info("Sending test SMS to: {}", phoneNumber);

        SmsResponse response = smsService.sendOtpSms(phoneNumber, "123456");

        return ResponseEntity.ok(
                ApiResponse.success("Test SMS sent successfully", response));
    }

    /**
     * Test order confirmation SMS
     */
    @PostMapping("/test/order-confirmation")
    public ResponseEntity<ApiResponse<SmsResponse>> sendOrderConfirmationSms(
            @RequestParam String phoneNumber) {

        log.info("Sending order confirmation SMS to: {}", phoneNumber);

        SmsResponse response = smsService.sendOrderConfirmationSms(
                phoneNumber,
                "IM-2026-TEST-001",
                "₹150.00"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Order confirmation SMS sent", response));
    }

    /**
     * Test payment success SMS
     */
    @PostMapping("/test/payment-success")
    public ResponseEntity<ApiResponse<SmsResponse>> sendPaymentSuccessSms(
            @RequestParam String phoneNumber) {

        log.info("Sending payment success SMS to: {}", phoneNumber);

        SmsResponse response = smsService.sendPaymentSuccessSms(
                phoneNumber,
                "IM-2026-TEST-001",
                "₹150.00"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Payment success SMS sent", response));
    }

    /**
     * Test OTP SMS
     */
    @PostMapping("/test/otp")
    public ResponseEntity<ApiResponse<SmsResponse>> sendOtpSms(
            @RequestParam String phoneNumber) {

        log.info("Sending OTP SMS to: {}", phoneNumber);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        SmsResponse response = smsService.sendOtpSms(phoneNumber, otp);

        return ResponseEntity.ok(
                ApiResponse.success("OTP SMS sent. OTP: " + otp, response));
    }

    /**
     * Test order status SMS
     */
    @PostMapping("/test/status")
    public ResponseEntity<ApiResponse<SmsResponse>> sendStatusSms(
            @RequestParam String phoneNumber,
            @RequestParam String status) {

        log.info("Sending status SMS: {}", status);

        SmsResponse response = smsService.sendOrderStatusSms(
                phoneNumber,
                "IM-2026-TEST-001",
                status
        );

        return ResponseEntity.ok(
                ApiResponse.success("Status SMS sent", response));
    }
}