package com.ironman.service;

import com.ironman.dto.request.CreateOrderRequest;
import com.ironman.dto.request.OrderAddonRequest;
import com.ironman.dto.request.OrderItemRequest;
import com.ironman.dto.response.*;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final LaundryServiceRepository laundryServiceRepository;
    private final ClothTypeRepository clothTypeRepository;
    private final AddOnRepository addOnRepository;
    private final SlotService slotService;
    private final PricingService pricingService;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");

    /**
     * Generate unique order number
     * Format: IM-2026-000001
     */
    private String generateOrderNumber() {
        int year = LocalDate.now().getYear();

        Optional<String> latestOrder = orderRepository.findLatestOrderNumber();

        long nextNumber = 1;
        if (latestOrder.isPresent()) {
            String latest = latestOrder.get();
            // Extract number part: IM-2026-000001 → 1
            String numberPart = latest.substring(latest.lastIndexOf("-") + 1);
            nextNumber = Long.parseLong(numberPart) + 1;
        }

        return String.format("IM-%d-%06d", year, nextNumber);
    }

    /**
     * Create a new order
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        // --- VALIDATIONS ---

        // Validate pickup address belongs to user
        Address pickupAddress = addressRepository.findByIdAndUserId(request.getPickupAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup address not found"));

        // Validate delivery address belongs to user
        Address deliveryAddress = addressRepository.findByIdAndUserId(request.getDeliveryAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));

        // Validate dates
        if (request.getPickupDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Pickup date cannot be in the past");
        }

        if (request.getExpectedDeliveryDate().isBefore(request.getPickupDate())) {
            throw new BadRequestException("Delivery date must be after pickup date");
        }

        // Validate and book slot
        slotService.validateSlot(request.getPickupDate(), request.getPickupSlot());

        // --- CALCULATE PRICING ---
        PricingResponse pricing = pricingService.calculatePricing(request);

        // --- CREATE ORDER ---

        // Fetch user
        User customer = new User();
        customer.setId(userId);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPickupAddress(pickupAddress);
        order.setDeliveryAddress(deliveryAddress);
        order.setPickupSlot(request.getPickupSlot());
        order.setPickupDate(request.getPickupDate());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setSubtotal(pricing.getSubtotal());
        order.setAddonCharges(pricing.getAddonCharges());
        order.setTaxAmount(pricing.getTaxAmount());
        order.setDiscountAmount(pricing.getDiscountAmount());
        order.setTotalAmount(pricing.getTotalAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        // --- CREATE ORDER ITEMS ---
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            LaundryService service = laundryServiceRepository.findById(itemRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

            ClothType clothType = clothTypeRepository.findById(itemRequest.getClothTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cloth type not found"));

            BigDecimal unitPrice = service.getBasePrice()
                    .multiply(clothType.getPriceMultiplier())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setService(service);
            item.setClothType(clothType);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);
            item.setNotes(itemRequest.getNotes());

            orderItems.add(item);
        }
        savedOrder.setItems(orderItems);

        // --- CREATE ORDER ADDONS ---
        List<OrderAddon> orderAddons = new ArrayList<>();
        if (request.getAddons() != null && !request.getAddons().isEmpty()) {
            for (OrderAddonRequest addonRequest : request.getAddons()) {
                AddOn addon = addOnRepository.findById(addonRequest.getAddonId())
                        .orElseThrow(() -> new ResourceNotFoundException("Add-on not found"));

                BigDecimal addonTotal = addon.getIsPerItem()
                        ? addon.getPrice().multiply(BigDecimal.valueOf(addonRequest.getQuantity()))
                        : addon.getPrice();

                OrderAddon orderAddon = new OrderAddon();
                orderAddon.setOrder(savedOrder);
                orderAddon.setAddon(addon);
                orderAddon.setQuantity(addonRequest.getQuantity());
                orderAddon.setPrice(addon.getPrice());
                orderAddon.setTotalPrice(addonTotal.setScale(2, RoundingMode.HALF_UP));

                orderAddons.add(orderAddon);
            }
        }
        savedOrder.setAddons(orderAddons);

        // Save order with items and addons
        Order finalOrder = orderRepository.save(savedOrder);

        // Book the slot
        slotService.bookSlot(request.getPickupDate(), request.getPickupSlot());

        log.info("Order created successfully: {}", finalOrder.getOrderNumber());

        return mapToOrderResponse(finalOrder);
    }

    /**
     * Get all orders for a customer
     */
    public List<OrderResponse> getMyOrders(Long userId, String status) {
        log.info("Fetching orders for user: {}, status: {}", userId, status);

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                    userId, OrderStatus.valueOf(status));
        } else {
            orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
        }

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get single order by ID
     */
    public OrderResponse getOrderById(Long userId, Long orderId) {
        log.info("Fetching order {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToOrderResponse(order);
    }

    /**
     * Cancel an order
     */
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        log.info("Cancelling order {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Can only cancel PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        // Release the slot
        slotService.cancelSlot(order.getPickupDate(), order.getPickupSlot());

        log.info("Order cancelled successfully: {}", order.getOrderNumber());

        return mapToOrderResponse(updatedOrder);
    }

    /**
     * Map Order entity to OrderResponse
     */
    private OrderResponse mapToOrderResponse(Order order) {
        // Map items
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .serviceName(item.getService().getName())
                        .clothTypeName(item.getClothType().getName())
                        .clothTypeCategory(item.getClothType().getCategory())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .notes(item.getNotes())
                        .build())
                .collect(Collectors.toList());

        // Map addons
        List<OrderAddonResponse> addonResponses = order.getAddons().stream()
                .map(addon -> OrderAddonResponse.builder()
                        .id(addon.getId())
                        .addonName(addon.getAddon().getName())
                        .quantity(addon.getQuantity())
                        .price(addon.getPrice())
                        .totalPrice(addon.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        // Format addresses
        String pickupAddr = formatAddress(order.getPickupAddress());
        String deliveryAddr = formatAddress(order.getDeliveryAddress());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .pickupAddress(pickupAddr)
                .deliveryAddress(deliveryAddr)
                .pickupSlot(order.getPickupSlot())
                .pickupDate(order.getPickupDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .items(itemResponses)
                .addons(addonResponses)
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

    /**
     * Format address to a readable string
     */
    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null) sb.append(", ").append(address.getAddressLine2());
        if (address.getLandmark() != null) sb.append(", ").append(address.getLandmark());
        sb.append(", ").append(address.getCity());
        sb.append(", ").append(address.getState());
        sb.append(" - ").append(address.getPincode());
        return sb.toString();
    }
}