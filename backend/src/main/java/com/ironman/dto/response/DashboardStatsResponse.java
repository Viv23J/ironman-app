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
public class DashboardStatsResponse {

    // Order Statistics
    private Long totalOrders;
    private Long pendingOrders;
    private Long activeOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    // Revenue Statistics
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;

    // User Statistics
    private Long totalCustomers;
    private Long newCustomersToday;
    private Long newCustomersWeek;
    private Long newCustomersMonth;

    // Partner Statistics
    private Long totalPartners;
    private Long approvedPartners;
    private Long pendingPartners;
    private Long activePartners;

    // Payment Statistics
    private Long totalPayments;
    private Long successfulPayments;
    private Long failedPayments;
    private BigDecimal averageOrderValue;
}