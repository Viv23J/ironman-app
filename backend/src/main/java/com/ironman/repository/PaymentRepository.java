package com.ironman.repository;

import com.ironman.model.Payment;
import com.ironman.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by Razorpay order ID
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // Find payment by our internal order ID
    Optional<Payment> findByOrderId(Long orderId);

    // Find all payments for a specific order
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    // Find payments by status
    List<Payment> findByStatus(PaymentStatus status);

    // Find payment by Razorpay payment ID
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
}