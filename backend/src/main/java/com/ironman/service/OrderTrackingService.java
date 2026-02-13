package com.ironman.service;

import com.ironman.dto.response.OrderTrackingResponse;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final AssignmentRepository assignmentRepository;
    private final DeliveryPartnerRepository partnerRepository;

    /**
     * Track order by ID
     */
    public OrderTrackingResponse trackOrder(Long orderId, Long userId) {
        log.info("Tracking order {} for user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getCustomer().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        return buildTrackingResponse(order);
    }

    /**
     * Track order by order number (public - no auth needed)
     */
    public OrderTrackingResponse trackOrderByNumber(String orderNumber) {
        log.info("Tracking order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        return buildTrackingResponse(order);
    }

    /**
     * Get order history for user
     */
    public List<OrderTrackingResponse> getOrderHistory(Long userId) {
        log.info("Fetching order history for user: {}", userId);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCustomer().getId().equals(userId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());

        return orders.stream()
                .map(this::buildTrackingResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    /**
     * Build complete tracking response
     */
    private OrderTrackingResponse buildTrackingResponse(Order order) {
        // Get status history
        List<OrderStatusHistory> history = statusHistoryRepository
                .findByOrderIdOrderByChangedAtAsc(order.getId());

        // Build timeline
        List<OrderTrackingResponse.StatusTimelineItem> timeline = buildStatusTimeline(order, history);

        // Get assigned partner info
        OrderTrackingResponse.PartnerInfo partnerInfo = getAssignedPartnerInfo(order.getId());

        // Calculate estimated delivery
        String estimatedDelivery = calculateEstimatedDelivery(order);
        Integer daysRemaining = calculateDaysRemaining(order);

        return OrderTrackingResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .currentStatus(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .pickupDate(order.getPickupDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualPickupTime(order.getActualPickupTime())
                .actualDeliveryTime(order.getActualDeliveryTime())
                .pickupAddress(formatAddress(order.getPickupAddress()))
                .deliveryAddress(formatAddress(order.getDeliveryAddress()))
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .discountAmount(order.getDiscountAmount())
                .statusTimeline(timeline)
                .assignedPartner(partnerInfo)
                .estimatedDeliveryTime(estimatedDelivery)
                .daysRemaining(daysRemaining)
                .build();
    }

    /**
     * Build status timeline with all status changes
     */
    private List<OrderTrackingResponse.StatusTimelineItem> buildStatusTimeline(
            Order order, List<OrderStatusHistory> history) {

        List<OrderTrackingResponse.StatusTimelineItem> timeline = new ArrayList<>();

        // Define all possible statuses in order
        List<OrderStatus> allStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.PICKUP_ASSIGNED,
                OrderStatus.PICKED_UP,
                OrderStatus.PROCESSING,
                OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.OUT_FOR_DELIVERY,
                OrderStatus.DELIVERED,
                OrderStatus.COMPLETED
        );

        OrderStatus currentStatus = order.getStatus();

        for (OrderStatus status : allStatuses) {
            // Find if this status exists in history
            Optional<OrderStatusHistory> historyItem = history.stream()
                    .filter(h -> h.getNewStatus() == status)
                    .findFirst();

            boolean completed = isStatusCompleted(status, currentStatus);

            OrderTrackingResponse.StatusTimelineItem item = OrderTrackingResponse.StatusTimelineItem.builder()
                    .status(status.name())
                    .description(getStatusDescription(status))
                    .completed(completed)
                    .timestamp(historyItem.map(OrderStatusHistory::getChangedAt).orElse(null))
                    .updatedBy(historyItem.map(OrderStatusHistory::getChangedBy).orElse(null))
                    .build();

            timeline.add(item);
        }

        return timeline;
    }

    /**
     * Check if status is completed based on current status
     */
    private boolean isStatusCompleted(OrderStatus status, OrderStatus currentStatus) {
        List<OrderStatus> statusOrder = List.of(
                OrderStatus.PENDING,
                OrderStatus.PICKUP_ASSIGNED,
                OrderStatus.PICKED_UP,
                OrderStatus.PROCESSING,
                OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.OUT_FOR_DELIVERY,
                OrderStatus.DELIVERED,
                OrderStatus.COMPLETED
        );

        int statusIndex = statusOrder.indexOf(status);
        int currentIndex = statusOrder.indexOf(currentStatus);

        return statusIndex <= currentIndex;
    }

    /**
     * Get human-readable status description
     */
    private String getStatusDescription(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "Order placed and awaiting payment";
            case PICKUP_ASSIGNED:
                return "Delivery partner assigned for pickup";
            case PICKED_UP:
                return "Your laundry has been picked up";
            case PROCESSING:
                return "Your clothes are being cleaned";
            case READY_FOR_DELIVERY:
                return "Order ready for delivery";
            case OUT_FOR_DELIVERY:
                return "Order is on the way to you";
            case DELIVERED:
                return "Order delivered successfully";
            case COMPLETED:
                return "Order completed";
            default:
                return status.name();
        }
    }

    /**
     * Get assigned partner information
     */
    private OrderTrackingResponse.PartnerInfo getAssignedPartnerInfo(Long orderId) {
        List<Assignment> assignments = assignmentRepository.findByOrderId(orderId);

        // Get the latest delivery assignment
        Optional<Assignment> deliveryAssignment = assignments.stream()
                .filter(a -> "DELIVERY".equals(a.getAssignmentType()))
                .findFirst();

        if (deliveryAssignment.isEmpty()) {
            // Try pickup assignment
            deliveryAssignment = assignments.stream()
                    .filter(a -> "PICKUP".equals(a.getAssignmentType()))
                    .findFirst();
        }

        if (deliveryAssignment.isPresent()) {
            DeliveryPartner partner = deliveryAssignment.get().getPartner();

            return OrderTrackingResponse.PartnerInfo.builder()
                    .partnerId(partner.getId())
                    .partnerName(partner.getUser().getFullName())
                    .partnerPhone(partner.getUser().getPhone())
                    .currentLatitude(partner.getCurrentLatitude())
                    .currentLongitude(partner.getCurrentLongitude())
                    .build();
        }

        return null;
    }

    /**
     * Calculate estimated delivery time
     */
    private String calculateEstimatedDelivery(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.COMPLETED) {
            return "Delivered";
        }

        if (order.getExpectedDeliveryDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate expected = order.getExpectedDeliveryDate();

            if (expected.isEqual(today)) {
                return "Today";
            } else if (expected.isEqual(today.plusDays(1))) {
                return "Tomorrow";
            } else {
                return expected.toString();
            }
        }

        return "To be confirmed";
    }

    /**
     * Calculate days remaining for delivery
     */
    private Integer calculateDaysRemaining(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.COMPLETED) {
            return 0;
        }

        if (order.getExpectedDeliveryDate() != null) {
            LocalDate today = LocalDate.now();
            long days = ChronoUnit.DAYS.between(today, order.getExpectedDeliveryDate());
            return (int) Math.max(0, days);
        }

        return null;
    }

    /**
     * Format address
     */
    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null) {
            sb.append(", ").append(address.getAddressLine2());
        }
        if (address.getLandmark() != null) {
            sb.append(", ").append(address.getLandmark());
        }
        sb.append(", ").append(address.getCity());
        sb.append(", ").append(address.getState());
        sb.append(" - ").append(address.getPincode());
        return sb.toString();
    }
}