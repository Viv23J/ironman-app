package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long partnerId;
    private String partnerName;
    private String assignmentType; // PICKUP or DELIVERY
    private String status;
    private String pickupAddress;
    private String deliveryAddress;
    private String customerPhone;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String notes;
    private LocalDateTime createdAt;
}