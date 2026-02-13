package com.ironman.service;

import com.ironman.dto.request.OrderSearchRequest;
import com.ironman.dto.response.OrderResponse;
import com.ironman.dto.response.PartnerResponse;
import com.ironman.dto.response.UserManagementResponse;
import com.ironman.model.*;
import com.ironman.repository.DeliveryPartnerRepository;
import com.ironman.repository.OrderRepository;
import com.ironman.repository.UserRepository;
import com.ironman.specification.OrderSpecification;
import com.ironman.specification.PartnerSpecification;
import com.ironman.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository partnerRepository;

    /**
     * Advanced order search with multiple filters
     */
    public Page<OrderResponse> searchOrders(OrderSearchRequest request) {
        log.info("Searching orders with filters: {}", request);

        // Parse status enums
        OrderStatus status = request.getStatus() != null && !request.getStatus().isEmpty()
                ? OrderStatus.valueOf(request.getStatus())
                : null;

        PaymentStatus paymentStatus = request.getPaymentStatus() != null && !request.getPaymentStatus().isEmpty()
                ? PaymentStatus.valueOf(request.getPaymentStatus())
                : null;

        // Build specification
        Specification<Order> spec = OrderSpecification.searchOrders(
                request.getOrderNumber(),
                status,
                paymentStatus,
                request.getStartDate(),
                request.getEndDate(),
                request.getCustomerPhone(),
                null
        );

        // Create pageable with sorting
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(request.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Execute search
        Page<Order> orders = orderRepository.findAll(spec, pageable);

        // Map to response
        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Search orders by customer
     */
    public Page<OrderResponse> searchOrdersByCustomer(Long customerId, int page, int size) {
        log.info("Searching orders for customer: {}", customerId);

        Specification<Order> spec = OrderSpecification.byCustomerId(customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Search users with filters
     */
    public Page<UserManagementResponse> searchUsers(
            String searchTerm,
            String role,
            LocalDate registeredAfter,
            LocalDate registeredBefore,
            int page,
            int size) {

        log.info("Searching users with term: {}, role: {}", searchTerm, role);

        UserRole userRole = role != null && !role.isEmpty()
                ? UserRole.valueOf(role)
                : null;

        LocalDateTime afterDateTime = registeredAfter != null ? registeredAfter.atStartOfDay() : null;
        LocalDateTime beforeDateTime = registeredBefore != null ? registeredBefore.atTime(23, 59, 59) : null;

        Specification<User> spec = UserSpecification.searchUsers(
                searchTerm,
                userRole,
                afterDateTime,
                beforeDateTime
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToUserManagementResponse);
    }

    /**
     * Search delivery partners with filters
     */
    public Page<PartnerResponse> searchPartners(
            String searchTerm,
            String status,
            Boolean isAvailable,
            String vehicleType,
            int page,
            int size) {

        log.info("Searching partners with term: {}, status: {}", searchTerm, status);

        PartnerStatus partnerStatus = status != null && !status.isEmpty()
                ? PartnerStatus.valueOf(status)
                : null;

        Specification<DeliveryPartner> spec = PartnerSpecification.searchPartners(
                searchTerm,
                partnerStatus,
                isAvailable,
                vehicleType
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<DeliveryPartner> partners = partnerRepository.findAll(spec, pageable);
        return partners.map(this::mapToPartnerResponse);
    }

    /**
     * Get available partners (shortcut method)
     */
    public List<PartnerResponse> getAvailablePartners() {
        log.info("Fetching available partners");

        Specification<DeliveryPartner> spec = PartnerSpecification.isAvailable();
        List<DeliveryPartner> partners = partnerRepository.findAll(spec);

        return partners.stream()
                .map(this::mapToPartnerResponse)
                .toList();
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

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
                .items(new ArrayList<>())
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
                .isActive(true)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(null)
                .build();
    }

    private PartnerResponse mapToPartnerResponse(DeliveryPartner partner) {
        return PartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .fullName(partner.getUser().getFullName())
                .phone(partner.getUser().getPhone())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .status(partner.getStatus().name())
                .isAvailable(partner.getIsAvailable())
                .currentLatitude(partner.getCurrentLatitude())
                .currentLongitude(partner.getCurrentLongitude())
                .rating(partner.getRating())
                .totalDeliveries(partner.getTotalDeliveries())
                .profileImageUrl(partner.getProfileImageUrl())
                .createdAt(partner.getCreatedAt())
                .build();
    }
}