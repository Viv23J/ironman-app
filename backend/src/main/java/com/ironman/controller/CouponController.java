package com.ironman.controller;

import com.ironman.dto.request.ApplyCouponRequest;
import com.ironman.dto.request.CouponRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.CouponResponse;
import com.ironman.dto.response.CouponValidationResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;

    /**
     * Create coupon (Admin)
     */
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {

        log.info("Creating coupon: {}", request.getCode());
        CouponResponse coupon = couponService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", coupon));
    }

    /**
     * Get all active coupons (Public/Customer)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getActiveCoupons() {

        log.info("Fetching all active coupons");
        List<CouponResponse> coupons = couponService.getAllActiveCoupons();

        return ResponseEntity.ok(
                ApiResponse.success("Active coupons fetched successfully", coupons));
    }

    /**
     * Get all coupons (Admin)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {

        log.info("Fetching all coupons");
        List<CouponResponse> coupons = couponService.getAllCoupons();

        return ResponseEntity.ok(
                ApiResponse.success("All coupons fetched successfully", coupons));
    }

    /**
     * Get coupon by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(
            @PathVariable String code) {

        log.info("Fetching coupon: {}", code);
        CouponResponse coupon = couponService.getCouponByCode(code);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon fetched successfully", coupon));
    }

    /**
     * Validate coupon
     */
    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable String code,
            @RequestParam BigDecimal orderAmount) {

        log.info("Validating coupon {} for user {}", code, currentUser.getId());
        CouponValidationResponse validation = couponService.validateCoupon(
                currentUser.getId(), code, orderAmount);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon validation completed", validation));
    }

    /**
     * Apply coupon to order
     */
    @PostMapping("/apply/{orderId}")
    public ResponseEntity<ApiResponse<String>> applyCoupon(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long orderId,
            @Valid @RequestBody ApplyCouponRequest request) {

        log.info("Applying coupon to order {} for user {}", orderId, currentUser.getId());
        couponService.applyCouponToOrder(orderId, currentUser.getId(), request.getCouponCode());

        return ResponseEntity.ok(
                ApiResponse.success("Coupon applied successfully", null));
    }

    /**
     * Update coupon (Admin)
     */
    @PutMapping("/admin/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponRequest request) {

        log.info("Updating coupon: {}", couponId);
        CouponResponse coupon = couponService.updateCoupon(couponId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon updated successfully", coupon));
    }

    /**
     * Deactivate coupon (Admin)
     */
    @PutMapping("/admin/{couponId}/deactivate")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivateCoupon(
            @PathVariable Long couponId) {

        log.info("Deactivating coupon: {}", couponId);
        CouponResponse coupon = couponService.deactivateCoupon(couponId);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon deactivated successfully", coupon));
    }
}