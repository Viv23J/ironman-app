package com.ironman.repository;

import com.ironman.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    long countByCouponIdAndUserId(Long couponId, Long userId);

    long countByCouponId(Long couponId);

    List<CouponUsage> findByUserId(Long userId);

    List<CouponUsage> findByCouponId(Long couponId);

    boolean existsByCouponIdAndOrderId(Long couponId, Long orderId);
}