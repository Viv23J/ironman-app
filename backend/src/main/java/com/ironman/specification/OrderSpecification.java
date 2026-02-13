package com.ironman.specification;

import com.ironman.model.Order;
import com.ironman.model.OrderStatus;
import com.ironman.model.PaymentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    /**
     * Search orders with multiple criteria
     */
    public static Specification<Order> searchOrders(
            String orderNumber,
            OrderStatus status,
            PaymentStatus paymentStatus,
            LocalDate startDate,
            LocalDate endDate,
            String customerPhone,
            Long customerId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by order number (partial match)
            if (orderNumber != null && !orderNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orderNumber")),
                        "%" + orderNumber.toLowerCase() + "%"
                ));
            }

            // Filter by order status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by payment status
            if (paymentStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus));
            }

            // Filter by date range
            if (startDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), startDateTime
                ));
            }

            if (endDate != null) {
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), endDateTime
                ));
            }

            // Filter by customer phone (join with customer)
            if (customerPhone != null && !customerPhone.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("customer").get("phone"),
                        "%" + customerPhone + "%"
                ));
            }

            // Filter by customer ID
            if (customerId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("customer").get("id"), customerId
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Search orders by customer
     */
    public static Specification<Order> byCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("customer").get("id"), customerId);
    }

    /**
     * Search orders by status
     */
    public static Specification<Order> byStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Search orders by date range
     */
    public static Specification<Order> byDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), startDate.atStartOfDay()
                ));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), endDate.atTime(23, 59, 59)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Search orders with minimum amount
     */
    public static Specification<Order> withMinimumAmount(Double minAmount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
    }

    /**
     * Search orders with coupon applied
     */
    public static Specification<Order> withCoupon() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("coupon"));
    }
}