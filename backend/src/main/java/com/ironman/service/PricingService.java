package com.ironman.service;

import com.ironman.dto.request.CreateOrderRequest;
import com.ironman.dto.request.OrderAddonRequest;
import com.ironman.dto.request.OrderItemRequest;
import com.ironman.dto.response.OrderAddonResponse;
import com.ironman.dto.response.OrderItemResponse;
import com.ironman.dto.response.PricingResponse;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.AddOn;
import com.ironman.model.ClothType;
import com.ironman.model.LaundryService;
import com.ironman.repository.AddOnRepository;
import com.ironman.repository.ClothTypeRepository;
import com.ironman.repository.LaundryServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final LaundryServiceRepository laundryServiceRepository;
    private final ClothTypeRepository clothTypeRepository;
    private final AddOnRepository addOnRepository;

    // Tax rate: 18% GST
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");

    /**
     * Calculate full pricing for an order request
     */
    public PricingResponse calculatePricing(CreateOrderRequest request) {
        log.info("Calculating pricing for order");

        // Calculate items
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // Fetch service
            LaundryService service = laundryServiceRepository.findById(itemRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Service not found with ID: " + itemRequest.getServiceId()));

            // Fetch cloth type
            ClothType clothType = clothTypeRepository.findById(itemRequest.getClothTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cloth type not found with ID: " + itemRequest.getClothTypeId()));

            // Calculate unit price: base_price * price_multiplier
            BigDecimal unitPrice = service.getBasePrice()
                    .multiply(clothType.getPriceMultiplier())
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate line total: unit_price * quantity
            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);

            itemResponses.add(OrderItemResponse.builder()
                    .serviceName(service.getName())
                    .clothTypeName(clothType.getName())
                    .clothTypeCategory(clothType.getCategory())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .notes(itemRequest.getNotes())
                    .build());
        }

        // Calculate add-ons
        List<OrderAddonResponse> addonResponses = new ArrayList<>();
        BigDecimal addonCharges = BigDecimal.ZERO;

        if (request.getAddons() != null && !request.getAddons().isEmpty()) {
            for (OrderAddonRequest addonRequest : request.getAddons()) {
                // Fetch addon
                AddOn addon = addOnRepository.findById(addonRequest.getAddonId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Add-on not found with ID: " + addonRequest.getAddonId()));

                BigDecimal addonTotal;
                if (addon.getIsPerItem()) {
                    // Per item: price * quantity
                    addonTotal = addon.getPrice()
                            .multiply(BigDecimal.valueOf(addonRequest.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);
                } else {
                    // Flat price
                    addonTotal = addon.getPrice();
                }

                addonCharges = addonCharges.add(addonTotal);

                addonResponses.add(OrderAddonResponse.builder()
                        .addonName(addon.getName())
                        .quantity(addonRequest.getQuantity())
                        .price(addon.getPrice())
                        .totalPrice(addonTotal)
                        .build());
            }
        }

        // Calculate tax on (subtotal + addon charges)
        BigDecimal taxableAmount = subtotal.add(addonCharges);
        BigDecimal taxAmount = taxableAmount
                .multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Discount (for now 0, can add coupon logic later)
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Total = subtotal + addonCharges + tax - discount
        BigDecimal totalAmount = subtotal
                .add(addonCharges)
                .add(taxAmount)
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Pricing calculated - Subtotal: {}, Addons: {}, Tax: {}, Total: {}",
                subtotal, addonCharges, taxAmount, totalAmount);

        return PricingResponse.builder()
                .subtotal(subtotal)
                .addonCharges(addonCharges)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .items(itemResponses)
                .addons(addonResponses)
                .build();
    }
}