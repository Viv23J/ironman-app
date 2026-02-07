package com.ironman.service;

import com.ironman.config.RazorpayConfig;
import com.ironman.dto.request.PaymentRequest;
import com.ironman.dto.request.PaymentVerificationRequest;
import com.ironman.dto.response.PaymentResponse;
import com.ironman.dto.response.RazorpayOrderResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.Order;
import com.ironman.model.OrderStatus;
import com.ironman.model.Payment;
import com.ironman.model.PaymentStatus;
import com.ironman.repository.OrderRepository;
import com.ironman.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RazorpayConfig razorpayConfig;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    // =============================================
    // STEP 1: CREATE RAZORPAY ORDER
    // =============================================

    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(PaymentRequest request) {
        log.info("Creating Razorpay order for internal order ID: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + request.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Payment can only be made for PENDING orders. Current: " + order.getStatus());
        }

        // Check if already paid
        Optional<Payment> existing = paymentRepository.findByOrderId(order.getId());
        if (existing.isPresent() && existing.get().getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment already completed for this order");
        }

        // Amount in paise (1 INR = 100 paise)
        int amountInPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        try {
            // Use Razorpay REST API directly via HTTP
            String razorpayOrderId = callRazorpayCreateOrder(
                    amountInPaise,
                    "INR",
                    order.getOrderNumber(),
                    "IronMan Laundry - " + order.getOrderNumber()
            );

            log.info("Razorpay order created: {}", razorpayOrderId);

            // Save payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency("INR");
            payment.setMethod("RAZORPAY");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            return RazorpayOrderResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .internalOrderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .amount(amountInPaise)
                    .currency("INR")
                    .razorpayKeyId(razorpayConfig.getKeyId())
                    .description("IronMan Laundry - " + order.getOrderNumber())
                    .build();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create Razorpay order", e);
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }

    // =============================================
    // STEP 2: VERIFY PAYMENT
    // =============================================

    @Transactional
    public PaymentResponse verifyPayment(PaymentVerificationRequest request) {
        log.info("Verifying payment - Razorpay Order ID: {}", request.getRazorpayOrderId());

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for Razorpay Order: " + request.getRazorpayOrderId()));

        // Verify signature: HMAC_SHA256(orderId|paymentId, keySecret)
        boolean isValid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed");
        }

        // ✅ Valid — mark PAID
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.PAID);
        Payment updated = paymentRepository.save(payment);

        // Update order
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PICKUP_ASSIGNED);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        // Send notification
        notificationService.notifyPaymentSuccess(
                order.getCustomer().getId(),
                order.getId(),
                order.getOrderNumber()
        );

        log.info("Payment VERIFIED for order: {}", order.getOrderNumber());
        return mapToPaymentResponse(updated);
    }

    // =============================================
    // STEP 3: WEBHOOK
    // =============================================

    @Transactional
    public void handleWebhook(String payload, String signature) {
        log.info("Webhook received");

        if (!verifyWebhookSignature(payload, signature)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        try {
            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");
            log.info("Webhook event: {}", eventType);

            if ("payment.authorized".equals(eventType)) {
                handlePaymentAuthorized(event);
            } else if ("payment.failed".equals(eventType)) {
                handlePaymentFailed(event);
            } else {
                log.info("Unhandled event: {}", eventType);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Webhook processing error", e);
            throw new BadRequestException("Webhook error: " + e.getMessage());
        }
    }

    // =============================================
    // PAYMENT HISTORY
    // =============================================

    public List<PaymentResponse> getPaymentHistory(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found for order: " + orderId));
        return mapToPaymentResponse(payment);
    }

    // =============================================
    // RAZORPAY API CALL (Direct HTTP)
    // =============================================

    /**
     * Calls Razorpay API directly using HttpURLConnection
     * This avoids issues with the Razorpay Java SDK
     */
    private String callRazorpayCreateOrder(int amount, String currency, String receipt, String description)
            throws Exception {

        String url = "https://api.razorpay.com/v1/orders";

        // Build JSON body
        JSONObject body = new JSONObject();
        body.put("amount", amount);
        body.put("currency", currency);
        body.put("receipt", receipt);
        body.put("description", description);

        // Basic Auth: KeyId:KeySecret
        String credentials = razorpayConfig.getKeyId() + ":" + razorpayConfig.getKeySecret();
        String basicAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        // HTTP POST
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", basicAuth);

        // Send body
        conn.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));
        conn.getOutputStream().flush();
        conn.getOutputStream().close();

        // Read response
        int statusCode = conn.getResponseCode();
        java.io.InputStream inputStream = (statusCode >= 200 && statusCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        conn.disconnect();

        if (statusCode != 200 && statusCode != 201) {
            log.error("Razorpay API error: {} - {}", statusCode, response);
            throw new BadRequestException("Razorpay API error: " + response);
        }

        // Parse order_id from response
        JSONObject responseJson = new JSONObject(response);
        return responseJson.getString("id");
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String expected = hmacSha256(payload, razorpayConfig.getKeySecret());
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String expected = hmacSha256(payload, razorpayConfig.getWebhookSecret());
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Webhook signature error", e);
            return false;
        }
    }

    private String hmacSha256(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private void handlePaymentAuthorized(JSONObject event) {
        JSONObject entity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = entity.getString("id");
        String orderId = entity.getString("order_id");

        log.info("Webhook AUTHORIZED — paymentId={}, orderId={}", paymentId, orderId);

        paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setRazorpayPaymentId(paymentId);
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                order.setStatus(OrderStatus.PICKUP_ASSIGNED);
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);

                log.info("Webhook: {} → PICKUP_ASSIGNED", order.getOrderNumber());
            }
        });
    }

    private void handlePaymentFailed(JSONObject event) {
        JSONObject entity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = entity.getString("id");
        String orderId = entity.getString("order_id");
        String reason = entity.optString("error_code", "Unknown");

        log.info("Webhook FAILED — paymentId={}, orderId={}", paymentId, orderId);

        paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
            payment.setRazorpayPaymentId(paymentId);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(reason);
            paymentRepository.save(payment);
            log.info("Webhook: Payment FAILED for {}", payment.getOrder().getOrderNumber());
        });
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .method(payment.getMethod())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}