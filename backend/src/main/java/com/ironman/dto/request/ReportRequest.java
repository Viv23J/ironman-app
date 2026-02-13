package com.ironman.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType; // REVENUE, ORDERS, PARTNERS
    private String format; // PDF, EXCEL
    private String groupBy; // DAILY, WEEKLY, MONTHLY
}