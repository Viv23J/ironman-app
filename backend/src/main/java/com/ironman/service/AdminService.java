package com.ironman.service;

import com.ironman.dto.request.OrderFilterRequest;
import com.ironman.dto.response.*;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final PaymentRepository paymentRepository;


    /**
     * Get dashboard statistics
     */
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard statistics");

        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime weekAgo = today.minusWeeks(1);
        LocalDateTime monthAgo = today.minusMonths(1);

        // Order Statistics
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long activeOrders = countActiveOrders();
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Revenue Statistics
        BigDecimal totalRevenue = calculateTotalRevenue();
        BigDecimal todayRevenue = calculateRevenueForPeriod(today, LocalDateTime.now());
        BigDecimal weekRevenue = calculateRevenueForPeriod(weekAgo, LocalDateTime.now());
        BigDecimal monthRevenue = calculateRevenueForPeriod(monthAgo, LocalDateTime.now());

        // User Statistics
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long newCustomersToday = countNewCustomersForPeriod(today, LocalDateTime.now());
        long newCustomersWeek = countNewCustomersForPeriod(weekAgo, LocalDateTime.now());
        long newCustomersMonth = countNewCustomersForPeriod(monthAgo, LocalDateTime.now());

        // Partner Statistics
        long totalPartners = partnerRepository.count();
        long approvedPartners = partnerRepository.countApprovedPartners();
        long pendingPartners = partnerRepository.findByStatus(PartnerStatus.PENDING_APPROVAL).size();
        long activePartners = partnerRepository.findByIsAvailableTrue().size();

        // Payment Statistics
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.findByStatus(PaymentStatus.PAID).size();
        long failedPayments = paymentRepository.findByStatus(PaymentStatus.FAILED).size();
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DashboardStatsResponse.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .activeOrders(activeOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)
                .totalCustomers(totalCustomers)
                .newCustomersToday(newCustomersToday)
                .newCustomersWeek(newCustomersWeek)
                .newCustomersMonth(newCustomersMonth)
                .totalPartners(totalPartners)
                .approvedPartners(approvedPartners)
                .pendingPartners(pendingPartners)
                .activePartners(activePartners)
                .totalPayments(totalPayments)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    /**
     * Get all orders with filtering and pagination
     */
    public Page<OrderResponse> getAllOrders(OrderFilterRequest filter) {
        log.info("Fetching all orders with filters: {}", filter);

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // For now, return all orders with pagination
        // TODO: Implement filtering logic with specifications
        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Update order status (Admin)
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        log.info("Updating order {} status to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus = OrderStatus.valueOf(status);
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        log.info("Order status updated successfully");
        return mapToOrderResponse(updated);
    }

    /**
     * Get all users with management info
     */
    public List<UserManagementResponse> getAllUsers(String role) {
        log.info("Fetching all users with role: {}", role);

        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(UserRole.valueOf(role));
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::mapToUserManagementResponse)
                .toList();
    }

    /**
     * Get revenue report for date range
     */
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report from {} to {}", startDate, endDate);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .toList();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Group by date
        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<RevenueReportResponse.DailyRevenue> dailyBreakdown = ordersByDate.entrySet().stream()
                .map(entry -> {
                    BigDecimal dayRevenue = entry.getValue().stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return RevenueReportResponse.DailyRevenue.builder()
                            .date(entry.getKey())
                            .revenue(dayRevenue)
                            .orderCount((long) entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(RevenueReportResponse.DailyRevenue::getDate))
                .collect(Collectors.toList());

        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    /**
     * Get order statistics for date range
     */
    public OrderStatsResponse getOrderStats(LocalDate startDate, LocalDate endDate) {
        log.info("Generating order stats from {} to {}", startDate, endDate);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .toList();

        long totalOrders = orders.size();

        // Group by status
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.counting()
                ));

        // Group by date
        Map<LocalDate, Long> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<OrderStatsResponse.DailyOrderCount> dailyBreakdown = ordersByDate.entrySet().stream()
                .map(entry -> OrderStatsResponse.DailyOrderCount.builder()
                        .date(entry.getKey())
                        .orderCount(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(OrderStatsResponse.DailyOrderCount::getDate))
                .collect(Collectors.toList());

        return OrderStatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalOrders(totalOrders)
                .ordersByStatus(ordersByStatus)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    private long countActiveOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.COMPLETED
                        && o.getStatus() != OrderStatus.CANCELLED)
                .count();
    }

    private BigDecimal calculateTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueForPeriod(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .filter(o -> !o.getCreatedAt().isBefore(start) && !o.getCreatedAt().isAfter(end))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countNewCustomersForPeriod(LocalDateTime start, LocalDateTime end) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .filter(u -> !u.getCreatedAt().isBefore(start) && !u.getCreatedAt().isAfter(end))
                .count();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .pickupAddress(formatAddress(order.getPickupAddress()))
                .deliveryAddress(formatAddress(order.getDeliveryAddress()))
                .pickupSlot(order.getPickupSlot())
                .pickupDate(order.getPickupDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .items(new ArrayList<>()) // Simplified for admin view
                .addons(new ArrayList<>())
                .subtotal(order.getSubtotal())
                .addonCharges(order.getAddonCharges())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .specialInstructions(order.getSpecialInstructions())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s - %s",
                address.getAddressLine1(),
                address.getCity(),
                address.getState(),
                address.getPincode());
    }

    private UserManagementResponse mapToUserManagementResponse(User user) {
        // Count orders for this user
        int totalOrders = (int) orderRepository.findAll().stream()
                .filter(o -> o.getCustomer().getId().equals(user.getId()))
                .count();

        return UserManagementResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole().name())
                .totalOrders(totalOrders)
                .isActive(true) // TODO: Add isActive field to User entity
                .createdAt(user.getCreatedAt())
                .lastLoginAt(null) // TODO: Track last login
                .build();
    }
}