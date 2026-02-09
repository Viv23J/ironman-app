package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalOrders;
    private Map<String, Long> ordersByStatus;
    private List<DailyOrderCount> dailyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyOrderCount {
        private LocalDate date;
        private Long orderCount;
    }
}