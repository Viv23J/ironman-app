package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.PartnerResponse;
import com.ironman.service.DeliveryPartnerService;
import com.ironman.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ironman.dto.request.OrderFilterRequest;
import com.ironman.dto.response.*;
import java.time.LocalDate;
import java.util.List;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final DeliveryPartnerService partnerService;
    private final AdminService adminService;
    /**
     * Get all pending approval partners
     */
    @GetMapping("/partners/pending")
    public ResponseEntity<ApiResponse<List<PartnerResponse>>> getPendingPartners() {

        log.info("Fetching pending approval partners");
        List<PartnerResponse> partners = partnerService.getPendingPartners();

        return ResponseEntity.ok(
                ApiResponse.success("Pending partners fetched successfully", partners));
    }

    /**
     * Approve partner
     */
    @PutMapping("/partners/{partnerId}/approve")
    public ResponseEntity<ApiResponse<PartnerResponse>> approvePartner(
            @PathVariable Long partnerId,
            @RequestParam(required = false) String notes) {

        log.info("Approving partner: {}", partnerId);
        PartnerResponse partner = partnerService.approvePartner(partnerId, notes);

        return ResponseEntity.ok(
                ApiResponse.success("Partner approved successfully", partner));
    }

    /**
     * Reject partner
     */
    @PutMapping("/partners/{partnerId}/reject")
    public ResponseEntity<ApiResponse<PartnerResponse>> rejectPartner(
            @PathVariable Long partnerId,
            @RequestParam String reason) {

        log.info("Rejecting partner: {}", partnerId);
        PartnerResponse partner = partnerService.rejectPartner(partnerId, reason);

        return ResponseEntity.ok(
                ApiResponse.success("Partner rejected", partner));
    }

    /**
     * Get partner details by ID
     */
    @GetMapping("/partners/{partnerId}")
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartnerById(
            @PathVariable Long partnerId) {

        log.info("Fetching partner: {}", partnerId);
        PartnerResponse partner = partnerService.getPartnerById(partnerId);

        return ResponseEntity.ok(
                ApiResponse.success("Partner fetched successfully", partner));
    }
    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {

        log.info("Fetching dashboard statistics");
        DashboardStatsResponse stats = adminService.getDashboardStats();

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard stats fetched successfully", stats));
    }

    /**
     * Get all orders with filters
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<OrderResponse>>> getAllOrders(
            OrderFilterRequest filter) {

        log.info("Fetching all orders");
        org.springframework.data.domain.Page<OrderResponse> orders = adminService.getAllOrders(filter);

        return ResponseEntity.ok(
                ApiResponse.success("Orders fetched successfully", orders));
    }

    /**
     * Update order status
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        log.info("Updating order {} status to {}", orderId, status);
        OrderResponse order = adminService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(
                ApiResponse.success("Order status updated successfully", order));
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getAllUsers(
            @RequestParam(required = false) String role) {

        log.info("Fetching all users");
        List<UserManagementResponse> users = adminService.getAllUsers(role);

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users));
    }

    /**
     * Get revenue report
     */
    @GetMapping("/reports/revenue")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Generating revenue report");
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        RevenueReportResponse report = adminService.getRevenueReport(start, end);

        return ResponseEntity.ok(
                ApiResponse.success("Revenue report generated successfully", report));
    }

    /**
     * Get order statistics
     */
    @GetMapping("/reports/orders")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> getOrderStats(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Generating order statistics");
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        OrderStatsResponse stats = adminService.getOrderStats(start, end);

        return ResponseEntity.ok(
                ApiResponse.success("Order stats generated successfully", stats));
    }
}
