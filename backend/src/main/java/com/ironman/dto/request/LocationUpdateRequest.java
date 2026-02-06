package com.ironman.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private BigDecimal accuracy;

    private Long assignmentId; // Optional - for tracking during delivery
}