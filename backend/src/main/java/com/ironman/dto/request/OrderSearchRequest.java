package com.ironman.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderSearchRequest {

    private String orderNumber;
    private String status;
    private String paymentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String customerPhone;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}