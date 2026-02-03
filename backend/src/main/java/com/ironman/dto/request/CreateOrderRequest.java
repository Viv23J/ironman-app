package com.ironman.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Pickup address ID is required")
    private Long pickupAddressId;

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;

    @NotNull(message = "Pickup slot is required")
    private String pickupSlot; // MORNING or EVENING

    @NotNull(message = "Pickup date is required")
    private LocalDate pickupDate;

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDeliveryDate;

    @NotEmpty(message = "At least one item is required")
    private List<@Valid OrderItemRequest> items;

    private List<@Valid OrderAddonRequest> addons;

    private String specialInstructions;

    private String paymentMethod = "RAZORPAY"; // Default payment method
}