package com.ironman.repository;

import com.ironman.model.Review;
import com.ironman.model.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByOrderId(Long orderId);

    List<Review> findByCustomerId(Long customerId);

    List<Review> findByPartnerId(Long partnerId);

    List<Review> findByPartnerIdAndIsApprovedTrue(Long partnerId);

    Optional<Review> findByOrderIdAndCustomerIdAndReviewType(Long orderId, Long customerId, ReviewType reviewType);

    boolean existsByOrderIdAndCustomerIdAndReviewType(Long orderId, Long customerId, ReviewType reviewType);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.partner.id = :partnerId AND r.reviewType = 'PARTNER_REVIEW' AND r.isApproved = true")
    Double getAveragePartnerRating(@Param("partnerId") Long partnerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.partner.id = :partnerId AND r.reviewType = 'PARTNER_REVIEW' AND r.isApproved = true")
    long countPartnerReviews(@Param("partnerId") Long partnerId);
}