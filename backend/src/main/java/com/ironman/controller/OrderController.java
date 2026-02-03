package com.ironman.controller;

import com.ironman.dto.request.CreateOrderRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.OrderResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // Create new order
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Creating order for user: {}", currentUser.getId());
        OrderResponse order = orderService.createOrder(currentUser.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    // Get all my orders
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(required = false) String status) {

        log.info("Fetching orders for user: {}", currentUser.getId());
        List<OrderResponse> orders = orderService.getMyOrders(currentUser.getId(), status);

        return ResponseEntity.ok(
                ApiResponse.success("Orders fetched successfully", orders));
    }

    // Get single order by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Fetching order {} for user: {}", id, currentUser.getId());
        OrderResponse order = orderService.getOrderById(currentUser.getId(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Order fetched successfully", order));
    }

    // Cancel order
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Cancelling order {} for user: {}", id, currentUser.getId());
        OrderResponse order = orderService.cancelOrder(currentUser.getId(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled successfully", order));
    }
}