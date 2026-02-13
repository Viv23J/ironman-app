package com.ironman.repository;

import com.ironman.model.Order;
import com.ironman.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    // Find order by ID and customer
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find all orders by customer
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Find orders by customer and status
    List<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, OrderStatus status);

    // Find orders by status
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    // Find orders by date
    List<Order> findByPickupDate(LocalDate pickupDate);

    // Count orders by status
    long countByStatus(OrderStatus status);

    // Get latest order number
    @Query("SELECT o.orderNumber FROM Order o ORDER BY o.id DESC LIMIT 1")
    Optional<String> findLatestOrderNumber();
}