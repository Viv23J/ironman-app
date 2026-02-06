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
public class LocationResponse {

    private Long id;
    private Long partnerId;
    private Long assignmentId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracy;
    private LocalDateTime recordedAt;
}