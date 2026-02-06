package com.ironman.controller;

import com.ironman.dto.request.LocationUpdateRequest;
import com.ironman.dto.request.PartnerRegistrationRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.LocationResponse;
import com.ironman.dto.response.PartnerResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Slf4j
public class DeliveryPartnerController {

    private final DeliveryPartnerService partnerService;

    /**
     * Register as a delivery partner
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PartnerResponse>> registerPartner(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody PartnerRegistrationRequest request) {

        log.info("Partner registration request from user: {}", currentUser.getId());
        PartnerResponse partner = partnerService.registerPartner(currentUser.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Partner registered successfully. Awaiting approval.", partner));
    }

    /**
     * Get my partner profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PartnerResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching partner profile for user: {}", currentUser.getId());
        PartnerResponse partner = partnerService.getPartnerProfile(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Partner profile fetched successfully", partner));
    }

    /**
     * Toggle availability (online/offline)
     */
    @PutMapping("/toggle-availability")
    public ResponseEntity<ApiResponse<PartnerResponse>> toggleAvailability(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Toggling availability for user: {}", currentUser.getId());
        PartnerResponse partner = partnerService.toggleAvailability(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Availability updated successfully", partner));
    }

    /**
     * Update location
     */
    @PostMapping("/location")
    public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody LocationUpdateRequest request) {

        log.info("Updating location for user: {}", currentUser.getId());
        LocationResponse location = partnerService.updateLocation(currentUser.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Location updated successfully", location));
    }

    /**
     * Get location history
     */
    @GetMapping("/location-history")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocationHistory(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching location history for user: {}", currentUser.getId());

        // Get partner profile first to get partner ID
        PartnerResponse partner = partnerService.getPartnerProfile(currentUser.getId());
        List<LocationResponse> history = partnerService.getLocationHistory(partner.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Location history fetched successfully", history));
    }

    /**
     * Get all available partners (for assignment)
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<PartnerResponse>>> getAvailablePartners() {

        log.info("Fetching available partners");
        List<PartnerResponse> partners = partnerService.getAvailablePartners();

        return ResponseEntity.ok(
                ApiResponse.success("Available partners fetched successfully", partners));
    }
}