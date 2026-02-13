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
public class InvoiceData {

    // Order details
    private String orderNumber;
    private LocalDateTime orderDate;
    private String orderStatus;

    // Customer details
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String billingAddress;

    // Pickup & Delivery
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDate pickupDate;
    private LocalDate expectedDeliveryDate;

    // Items
    private List<InvoiceItem> items;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal addonCharges;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private BigDecimal totalAmount;

    // Payment
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paymentDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItem {
        private String serviceName;
        private String clothType;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}