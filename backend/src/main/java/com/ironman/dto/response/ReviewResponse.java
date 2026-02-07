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
public class ReviewResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private Long partnerId;
    private String partnerName;
    private String reviewType;
    private Integer rating;
    private String comment;
    private Boolean isApproved;
    private LocalDateTime createdAt;
}