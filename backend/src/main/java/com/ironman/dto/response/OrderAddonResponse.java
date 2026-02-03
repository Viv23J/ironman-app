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
public class OrderAddonResponse {

    private Long id;
    private String addonName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}