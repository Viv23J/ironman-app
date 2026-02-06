package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String vehicleType;
    private String vehicleNumber;
    private String status;
    private Boolean isAvailable;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal rating;
    private Integer totalDeliveries;
    private String profileImageUrl;
    private LocalDateTime createdAt;
}