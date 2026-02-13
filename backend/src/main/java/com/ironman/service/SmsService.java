package com.ironman.service;

import com.ironman.config.TwilioConfig;
import com.ironman.dto.request.SmsRequest;
import com.ironman.dto.response.SmsResponse;
import com.ironman.model.SmsLog;
import com.ironman.repository.SmsLogRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final TwilioConfig twilioConfig;
    private final SmsLogRepository smsLogRepository;

    /**
     * Send SMS
     */
    @Transactional
    public SmsResponse sendSms(SmsRequest request) {
        log.info("Sending SMS to: {}", request.getPhoneNumber());

        SmsLog smsLog = new SmsLog();
        smsLog.setPhoneNumber(request.getPhoneNumber());
        smsLog.setMessage(request.getMessage());
        smsLog.setSmsType(request.getSmsType());
        smsLog.setOrderId(request.getOrderId());

        try {
            // Send SMS via Twilio
            Message message = Message.creator(
                    new PhoneNumber(request.getPhoneNumber()),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    request.getMessage()
            ).create();

            // Log success
            smsLog.setTwilioSid(message.getSid());
            smsLog.setStatus("SENT");

            log.info("SMS sent successfully. SID: {}", message.getSid());

        } catch (Exception e) {
            // Log failure
            smsLog.setStatus("FAILED");
            smsLog.setErrorMessage(e.getMessage());
            log.error("Failed to send SMS: {}", e.getMessage());
        }

        SmsLog saved = smsLogRepository.save(smsLog);
        return mapToSmsResponse(saved);
    }

    /**
     * Send order confirmation SMS
     */
    public SmsResponse sendOrderConfirmationSms(String phoneNumber, String orderNumber, String totalAmount) {
        log.info("Sending order confirmation SMS for order: {}", orderNumber);

        String message = String.format(
                "IronMan Laundry: Your order %s has been confirmed! Total: %s. " +
                        "We'll notify you when it's picked up. Thank you!",
                orderNumber, totalAmount
        );

        SmsRequest request = SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .smsType("ORDER_CONFIRMATION")
                .build();

        return sendSms(request);
    }

    /**
     * Send payment success SMS
     */
    public SmsResponse sendPaymentSuccessSms(String phoneNumber, String orderNumber, String amount) {
        log.info("Sending payment success SMS for order: {}", orderNumber);

        String message = String.format(
                "IronMan Laundry: Payment of %s received for order %s. " +
                        "Your order will be processed shortly. Thank you!",
                amount, orderNumber
        );

        SmsRequest request = SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .smsType("PAYMENT_SUCCESS")
                .build();

        return sendSms(request);
    }

    /**
     * Send OTP SMS
     */
    public SmsResponse sendOtpSms(String phoneNumber, String otp) {
        log.info("Sending OTP SMS to: {}", phoneNumber);

        String message = String.format(
                "Your IronMan Laundry verification code is: %s. " +
                        "Valid for 10 minutes. Do not share this code with anyone.",
                otp
        );

        SmsRequest request = SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .smsType("OTP")
                .build();

        return sendSms(request);
    }

    /**
     * Send order status update SMS
     */
    public SmsResponse sendOrderStatusSms(String phoneNumber, String orderNumber, String status) {
        log.info("Sending order status update SMS: {} - {}", orderNumber, status);

        String message = getStatusMessage(orderNumber, status);

        SmsRequest request = SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .smsType("STATUS_UPDATE")
                .build();

        return sendSms(request);
    }

    /**
     * Get SMS logs by phone number
     */
    public List<SmsResponse> getSmsByPhoneNumber(String phoneNumber) {
        log.info("Fetching SMS logs for: {}", phoneNumber);

        List<SmsLog> logs = smsLogRepository.findByPhoneNumber(phoneNumber);
        return logs.stream()
                .map(this::mapToSmsResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get SMS logs by order
     */
    public List<SmsResponse> getSmsByOrder(Long orderId) {
        log.info("Fetching SMS logs for order: {}", orderId);

        List<SmsLog> logs = smsLogRepository.findByOrderId(orderId);
        return logs.stream()
                .map(this::mapToSmsResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    /**
     * Get status-specific message
     */
    private String getStatusMessage(String orderNumber, String status) {
        switch (status.toUpperCase()) {
            case "PICKED_UP":
                return String.format("IronMan Laundry: Your order %s has been picked up and is now being processed.", orderNumber);

            case "PROCESSING":
                return String.format("IronMan Laundry: Your order %s is being processed at our facility.", orderNumber);

            case "READY_FOR_DELIVERY":
                return String.format("IronMan Laundry: Your order %s is ready and will be delivered soon!", orderNumber);

            case "OUT_FOR_DELIVERY":
                return String.format("IronMan Laundry: Your order %s is out for delivery. You'll receive it soon!", orderNumber);

            case "DELIVERED":
                return String.format("IronMan Laundry: Your order %s has been delivered. Thank you for choosing us!", orderNumber);

            default:
                return String.format("IronMan Laundry: Order %s status updated to: %s", orderNumber, status);
        }
    }

    /**
     * Map SmsLog to SmsResponse
     */
    private SmsResponse mapToSmsResponse(SmsLog log) {
        return SmsResponse.builder()
                .id(log.getId())
                .phoneNumber(log.getPhoneNumber())
                .message(log.getMessage())
                .smsType(log.getSmsType())
                .twilioSid(log.getTwilioSid())
                .status(log.getStatus())
                .sentAt(log.getSentAt())
                .build();
    }
}