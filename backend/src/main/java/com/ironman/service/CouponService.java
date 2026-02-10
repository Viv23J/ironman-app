package com.ironman.service;

import com.ironman.dto.request.ApplyCouponRequest;
import com.ironman.dto.request.CouponRequest;
import com.ironman.dto.response.CouponResponse;
import com.ironman.dto.response.CouponValidationResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.CouponRepository;
import com.ironman.repository.CouponUsageRepository;
import com.ironman.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final OrderRepository orderRepository;

    /**
     * Create a new coupon (Admin)
     */
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        log.info("Creating coupon with code: {}", request.getCode());

        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists");
        }

        // Validate dates
        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Valid until date must be after valid from date");
        }

        // Create coupon
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(DiscountType.valueOf(request.getDiscountType()));
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderValue(request.getMinOrderValue());
        coupon.setMaxUsageCount(request.getMaxUsageCount());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        coupon.setCurrentUsageCount(0);
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setIsActive(true);
        coupon.setFirstOrderOnly(request.getFirstOrderOnly());

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created successfully: {}", saved.getCode());

        return mapToCouponResponse(saved);
    }

    /**
     * Validate and calculate discount for a coupon
     */
    public CouponValidationResponse validateCoupon(Long userId, String couponCode, BigDecimal orderAmount) {
        log.info("Validating coupon {} for user {} with order amount {}", couponCode, userId, orderAmount);

        // Find coupon
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(couponCode.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid or inactive coupon code"));

        // Check if coupon is valid
        LocalDateTime now = LocalDateTime.now();

        // Check validity period
        if (now.isBefore(coupon.getValidFrom())) {
            return buildInvalidResponse(couponCode, "Coupon is not yet valid");
        }

        if (now.isAfter(coupon.getValidUntil())) {
            return buildInvalidResponse(couponCode, "Coupon has expired");
        }

        // Check minimum order value
        if (coupon.getMinOrderValue() != null && orderAmount.compareTo(coupon.getMinOrderValue()) < 0) {
            return buildInvalidResponse(couponCode,
                    String.format("Minimum order value of ₹%.2f required", coupon.getMinOrderValue()));
        }

        // Check total usage limit
        if (coupon.getMaxUsageCount() != null &&
                coupon.getCurrentUsageCount() >= coupon.getMaxUsageCount()) {
            return buildInvalidResponse(couponCode, "Coupon usage limit reached");
        }

        // Check per-user usage limit
        long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
        if (userUsageCount >= coupon.getMaxUsagePerUser()) {
            return buildInvalidResponse(couponCode, "You have already used this coupon maximum times");
        }

        // Check first order only
        if (coupon.getFirstOrderOnly()) {
            long userOrderCount = orderRepository.findAll().stream()
                    .filter(o -> o.getCustomer().getId().equals(userId))
                    .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                    .count();

            if (userOrderCount > 0) {
                return buildInvalidResponse(couponCode, "This coupon is valid only for first order");
            }
        }

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        log.info("Coupon validated successfully. Discount: {}", discountAmount);

        return CouponValidationResponse.builder()
                .valid(true)
                .message("Coupon applied successfully")
                .couponCode(coupon.getCode())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    /**
     * Apply coupon to order
     */
    @Transactional
    public void applyCouponToOrder(Long orderId, Long userId, String couponCode) {
        log.info("Applying coupon {} to order {}", couponCode, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getCustomer().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to user");
        }

        // Check if coupon already applied
        if (order.getCoupon() != null) {
            throw new BadRequestException("A coupon is already applied to this order");
        }

        // Validate coupon
        CouponValidationResponse validation = validateCoupon(userId, couponCode, order.getTotalAmount());

        if (!validation.getValid()) {
            throw new BadRequestException(validation.getMessage());
        }

        // Get coupon
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(couponCode.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Coupon not found"));

        // Update order
        order.setCoupon(coupon);
        order.setCouponCode(coupon.getCode());
        order.setDiscountAmount(validation.getDiscountAmount());
        order.setTotalAmount(validation.getFinalAmount());
        orderRepository.save(order);

        // Record usage
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setUser(order.getCustomer());
        usage.setOrder(order);
        usage.setDiscountAmount(validation.getDiscountAmount());
        couponUsageRepository.save(usage);

        // Increment usage count
        coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
        couponRepository.save(coupon);

        log.info("Coupon applied successfully to order");
    }

    /**
     * Get all active coupons
     */
    public List<CouponResponse> getAllActiveCoupons() {
        log.info("Fetching all active coupons");

        List<Coupon> coupons = couponRepository.findByIsActiveTrue();

        return coupons.stream()
                .filter(c -> c.getValidUntil().isAfter(LocalDateTime.now()))
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all coupons (Admin)
     */
    public List<CouponResponse> getAllCoupons() {
        log.info("Fetching all coupons");

        List<Coupon> coupons = couponRepository.findAll();
        return coupons.stream()
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get coupon by code
     */
    public CouponResponse getCouponByCode(String code) {
        log.info("Fetching coupon: {}", code);

        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        return mapToCouponResponse(coupon);
    }

    /**
     * Update coupon (Admin)
     */
    @Transactional
    public CouponResponse updateCoupon(Long couponId, CouponRequest request) {
        log.info("Updating coupon: {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        // Check if code is being changed and if it conflicts
        if (!coupon.getCode().equals(request.getCode().toUpperCase())) {
            if (couponRepository.existsByCode(request.getCode())) {
                throw new BadRequestException("Coupon code already exists");
            }
            coupon.setCode(request.getCode().toUpperCase());
        }

        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(DiscountType.valueOf(request.getDiscountType()));
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderValue(request.getMinOrderValue());
        coupon.setMaxUsageCount(request.getMaxUsageCount());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setFirstOrderOnly(request.getFirstOrderOnly());

        Coupon updated = couponRepository.save(coupon);
        log.info("Coupon updated successfully");

        return mapToCouponResponse(updated);
    }

    /**
     * Deactivate coupon (Admin)
     */
    @Transactional
    public CouponResponse deactivateCoupon(Long couponId) {
        log.info("Deactivating coupon: {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.setIsActive(false);
        Coupon updated = couponRepository.save(coupon);

        log.info("Coupon deactivated");
        return mapToCouponResponse(updated);
    }

    /**
     * Get coupon usage statistics
     */
    public List<CouponUsage> getCouponUsageStats(Long couponId) {
        log.info("Fetching usage stats for coupon: {}", couponId);
        return couponUsageRepository.findByCouponId(couponId);
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    /**
     * Calculate discount amount based on coupon type
     */
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;

        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                // Apply max discount cap if set
                if (coupon.getMaxDiscountAmount() != null &&
                        discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discount = coupon.getMaxDiscountAmount();
                }
                break;

            case FIXED_AMOUNT:
                discount = coupon.getDiscountValue();

                // Discount cannot exceed order amount
                if (discount.compareTo(orderAmount) > 0) {
                    discount = orderAmount;
                }
                break;

            default:
                discount = BigDecimal.ZERO;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Build invalid coupon response
     */
    private CouponValidationResponse buildInvalidResponse(String couponCode, String message) {
        return CouponValidationResponse.builder()
                .valid(false)
                .message(message)
                .couponCode(couponCode)
                .build();
    }

    /**
     * Map Coupon to CouponResponse
     */
    private CouponResponse mapToCouponResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderValue(coupon.getMinOrderValue())
                .maxUsageCount(coupon.getMaxUsageCount())
                .maxUsagePerUser(coupon.getMaxUsagePerUser())
                .currentUsageCount(coupon.getCurrentUsageCount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .isActive(coupon.getIsActive())
                .firstOrderOnly(coupon.getFirstOrderOnly())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}