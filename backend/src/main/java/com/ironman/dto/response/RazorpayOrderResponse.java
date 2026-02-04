package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {

    private String razorpayOrderId;     // Razorpay order ID (order_xxxxx)
    private Long internalOrderId;       // Our order ID
    private String orderNumber;         // Our order number (IM-2026-000001)
    private int amount;                 // Amount in paise (multiply by 100)
    private String currency;            // INR
    private String razorpayKeyId;       // Razorpay Key ID (for frontend)
    private String description;         // Order description
}