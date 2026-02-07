package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRatingResponse {

    private Long partnerId;
    private String partnerName;
    private BigDecimal averageRating;
    private Long totalReviews;
}