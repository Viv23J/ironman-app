package com.ironman.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderFilterRequest {

    private String status;           // PENDING, DELIVERED, etc.
    private String paymentStatus;    // PAID, PENDING, etc.
    private LocalDate startDate;
    private LocalDate endDate;
    private String customerPhone;
    private String orderNumber;
    private Integer page = 0;
    private Integer size = 20;
}