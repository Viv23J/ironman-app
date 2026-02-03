package com.ironman.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderAddonRequest {

    @NotNull(message = "Addon ID is required")
    private Long addonId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
}