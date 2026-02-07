package com.ironman.controller;

import com.ironman.dto.request.ReviewRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.PartnerRatingResponse;
import com.ironman.dto.response.ReviewResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Create a review
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody ReviewRequest request) {

        log.info("Creating review for order {} by user {}", request.getOrderId(), currentUser.getId());
        ReviewResponse review = reviewService.createReview(currentUser.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", review));
    }

    /**
     * Get reviews for an order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getOrderReviews(
            @PathVariable Long orderId) {

        log.info("Fetching reviews for order: {}", orderId);
        List<ReviewResponse> reviews = reviewService.getOrderReviews(orderId);

        return ResponseEntity.ok(
                ApiResponse.success("Reviews fetched successfully", reviews));
    }

    /**
     * Get my reviews (as a customer)
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching reviews by user: {}", currentUser.getId());
        List<ReviewResponse> reviews = reviewService.getCustomerReviews(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Your reviews fetched successfully", reviews));
    }

    /**
     * Get reviews for a partner
     */
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getPartnerReviews(
            @PathVariable Long partnerId) {

        log.info("Fetching reviews for partner: {}", partnerId);
        List<ReviewResponse> reviews = reviewService.getPartnerReviews(partnerId);

        return ResponseEntity.ok(
                ApiResponse.success("Partner reviews fetched successfully", reviews));
    }

    /**
     * Get partner rating summary
     */
    @GetMapping("/partner/{partnerId}/rating")
    public ResponseEntity<ApiResponse<PartnerRatingResponse>> getPartnerRating(
            @PathVariable Long partnerId) {

        log.info("Fetching rating for partner: {}", partnerId);
        PartnerRatingResponse rating = reviewService.getPartnerRating(partnerId);

        return ResponseEntity.ok(
                ApiResponse.success("Partner rating fetched successfully", rating));
    }

    /**
     * Moderate review (Admin)
     */
    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam boolean approve) {

        log.info("Moderating review {}: {}", reviewId, approve);
        ReviewResponse review = reviewService.moderateReview(reviewId, approve);

        return ResponseEntity.ok(
                ApiResponse.success("Review moderated successfully", review));
    }
}