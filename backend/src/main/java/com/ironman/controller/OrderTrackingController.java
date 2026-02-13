package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.OrderTrackingResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingController {

    private final OrderTrackingService trackingService;

    /**
     * Track order by ID (authenticated)
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long orderId) {

        log.info("Tracking order {} for user {}", orderId, currentUser.getId());
        OrderTrackingResponse tracking = trackingService.trackOrder(orderId, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Order tracking information", tracking));
    }

    /**
     * Track order by order number (public - no auth)
     */
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrderByNumber(
            @PathVariable String orderNumber) {

        log.info("Tracking order by number: {}", orderNumber);
        OrderTrackingResponse tracking = trackingService.trackOrderByNumber(orderNumber);

        return ResponseEntity.ok(
                ApiResponse.success("Order tracking information", tracking));
    }

    /**
     * Get order history (all orders for user)
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<OrderTrackingResponse>>> getOrderHistory(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching order history for user: {}", currentUser.getId());
        List<OrderTrackingResponse> history = trackingService.getOrderHistory(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Order history fetched successfully", history));
    }
}