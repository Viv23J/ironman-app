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
public class OrderTrackingResponse {

    private Long orderId;
    private String orderNumber;
    private String currentStatus;
    private String paymentStatus;

    // Dates
    private LocalDate pickupDate;
    private LocalDate expectedDeliveryDate;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualDeliveryTime;

    // Address
    private String pickupAddress;
    private String deliveryAddress;

    // Payment
    private BigDecimal totalAmount;
    private String couponCode;
    private BigDecimal discountAmount;

    // Timeline
    private List<StatusTimelineItem> statusTimeline;

    // Partner info (if assigned)
    private PartnerInfo assignedPartner;

    // Estimated time
    private String estimatedDeliveryTime;
    private Integer daysRemaining;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusTimelineItem {
        private String status;
        private LocalDateTime timestamp;
        private String description;
        private String updatedBy;
        private boolean completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerInfo {
        private Long partnerId;
        private String partnerName;
        private String partnerPhone;
        private BigDecimal currentLatitude;
        private BigDecimal currentLongitude;
    }
}