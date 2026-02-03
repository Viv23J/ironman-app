package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String status;
    private String paymentStatus;

    // Addresses
    private String pickupAddress;
    private String deliveryAddress;

    // Scheduling
    private String pickupSlot;
    private LocalDate pickupDate;
    private LocalDate expectedDeliveryDate;

    // Items
    private List<OrderItemResponse> items;
    private List<OrderAddonResponse> addons;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal addonCharges;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // Instructions
    private String specialInstructions;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}