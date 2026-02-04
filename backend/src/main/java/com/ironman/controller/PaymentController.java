package com.ironman.controller;

import com.ironman.dto.request.PaymentRequest;
import com.ironman.dto.request.PaymentVerificationRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.PaymentResponse;
import com.ironman.dto.response.RazorpayOrderResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * STEP 1: Create Razorpay order
     * Frontend calls this first to get Razorpay order ID
     * Then initializes Razorpay checkout with that ID
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createPaymentOrder(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody PaymentRequest request) {

        log.info("Creating payment order for user: {}", currentUser.getId());
        RazorpayOrderResponse razorpayOrder = paymentService.createRazorpayOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Razorpay order created", razorpayOrder));
    }

    /**
     * STEP 2: Verify payment
     * Frontend calls this after successful Razorpay checkout
     * Sends back the signature for backend verification
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody PaymentVerificationRequest request) {

        log.info("Verifying payment for user: {}", currentUser.getId());
        PaymentResponse payment = paymentService.verifyPayment(request);

        return ResponseEntity.ok(
                ApiResponse.success("Payment verified successfully", payment));
    }

    /**
     * STEP 3: Razorpay Webhook endpoint
     * Razorpay calls this automatically on payment events
     * This is PUBLIC - no auth needed (Razorpay calls it directly)
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Webhook received from Razorpay");
        paymentService.handleWebhook(payload, signature);

        return ResponseEntity.ok(
                ApiResponse.success("Webhook processed successfully", null));
    }

    /**
     * Get payment details for an order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long orderId) {

        log.info("Fetching payment for order: {}", orderId);
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);

        return ResponseEntity.ok(
                ApiResponse.success("Payment fetched successfully", payment));
    }

    /**
     * Get full payment history for an order
     */
    @GetMapping("/history/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long orderId) {

        log.info("Fetching payment history for order: {}", orderId);
        List<PaymentResponse> history = paymentService.getPaymentHistory(orderId);

        return ResponseEntity.ok(
                ApiResponse.success("Payment history fetched", history));
    }
}