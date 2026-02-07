package com.ironman.service;

import com.ironman.dto.request.ReviewRequest;
import com.ironman.dto.response.PartnerRatingResponse;
import com.ironman.dto.response.ReviewResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.DeliveryPartnerRepository;
import com.ironman.repository.OrderRepository;
import com.ironman.repository.ReviewRepository;
import com.ironman.repository.UserRepository;
import com.ironman.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final AssignmentRepository assignmentRepository;

    /**
     * Create a review
     */
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        log.info("Creating review for order {} by user {}", request.getOrderId(), userId);

        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user is the customer of this order
        if (!order.getCustomer().getId().equals(userId)) {
            throw new BadRequestException("You can only review your own orders");
        }

        // Check order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("You can only review delivered orders");
        }

        // Parse review type
        ReviewType reviewType = ReviewType.valueOf(request.getReviewType());

        // Check if review already exists
        if (reviewRepository.existsByOrderIdAndCustomerIdAndReviewType(
                request.getOrderId(), userId, reviewType)) {
            throw new BadRequestException("You have already submitted this type of review for this order");
        }

        // Get customer
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create review
        Review review = new Review();
        review.setOrder(order);
        review.setCustomer(customer);
        review.setReviewType(reviewType);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsApproved(true); // Auto-approve for now

        // If partner review, link to partner
        if (reviewType == ReviewType.PARTNER_REVIEW) {
            // Find the delivery partner from assignments
            DeliveryPartner partner = findPartnerForOrder(order.getId());
            if (partner == null) {
                throw new BadRequestException("No delivery partner assigned to this order yet. Partner reviews are only available after delivery.");
            }
            review.setPartner(partner);
        }

        Review saved = reviewRepository.save(review);

        // Update partner rating if it's a partner review
        if (reviewType == ReviewType.PARTNER_REVIEW && saved.getPartner() != null) {
            updatePartnerRating(saved.getPartner().getId());
        }

        log.info("Review created successfully: {}", saved.getId());
        return mapToReviewResponse(saved);
    }

    /**
     * Get reviews for an order
     */
    public List<ReviewResponse> getOrderReviews(Long orderId) {
        log.info("Fetching reviews for order: {}", orderId);

        List<Review> reviews = reviewRepository.findByOrderId(orderId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reviews by customer
     */
    public List<ReviewResponse> getCustomerReviews(Long userId) {
        log.info("Fetching reviews by customer: {}", userId);

        List<Review> reviews = reviewRepository.findByCustomerId(userId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reviews for a partner
     */
    public List<ReviewResponse> getPartnerReviews(Long partnerId) {
        log.info("Fetching reviews for partner: {}", partnerId);

        List<Review> reviews = reviewRepository.findByPartnerIdAndIsApprovedTrue(partnerId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get partner rating summary
     */
    public PartnerRatingResponse getPartnerRating(Long partnerId) {
        log.info("Fetching rating for partner: {}", partnerId);

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Double avgRating = reviewRepository.getAveragePartnerRating(partnerId);
        long totalReviews = reviewRepository.countPartnerReviews(partnerId);

        BigDecimal average = avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return PartnerRatingResponse.builder()
                .partnerId(partnerId)
                .partnerName(partner.getUser().getFullName())
                .averageRating(average)
                .totalReviews(totalReviews)
                .build();
    }

    /**
     * Moderate review (Admin)
     */
    @Transactional
    public ReviewResponse moderateReview(Long reviewId, boolean approve) {
        log.info("Moderating review {}: {}", reviewId, approve ? "APPROVE" : "REJECT");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setIsApproved(approve);
        Review updated = reviewRepository.save(review);

        // Recalculate partner rating
        if (review.getPartner() != null) {
            updatePartnerRating(review.getPartner().getId());
        }

        return mapToReviewResponse(updated);
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    /**
     * Find delivery partner for an order (from pickup or delivery assignment)
     */
    private DeliveryPartner findPartnerForOrder(Long orderId) {
        // Query assignments to find the delivery partner
        List<Assignment> assignments = assignmentRepository.findByOrderId(orderId);

        // Look for delivery assignment first, then pickup
        return assignments.stream()
                .filter(a -> "DELIVERY".equals(a.getAssignmentType()) || "PICKUP".equals(a.getAssignmentType()))
                .findFirst()
                .map(Assignment::getPartner)
                .orElse(null);
    }

    /**
     * Update partner's average rating
     */
    private void updatePartnerRating(Long partnerId) {
        Double avgRating = reviewRepository.getAveragePartnerRating(partnerId);

        if (avgRating != null) {
            DeliveryPartner partner = partnerRepository.findById(partnerId).orElse(null);
            if (partner != null) {
                partner.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
                partnerRepository.save(partner);
                log.info("Updated partner {} rating to {}", partnerId, partner.getRating());
            }
        }
    }

    /**
     * Map Review to ReviewResponse
     */
    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .orderNumber(review.getOrder().getOrderNumber())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFullName())
                .partnerId(review.getPartner() != null ? review.getPartner().getId() : null)
                .partnerName(review.getPartner() != null ? review.getPartner().getUser().getFullName() : null)
                .reviewType(review.getReviewType().name())
                .rating(review.getRating())
                .comment(review.getComment())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt())
                .build();
    }
}