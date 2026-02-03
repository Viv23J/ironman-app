package com.ironman.controller;

import com.ironman.dto.request.AddressRequest;
import com.ironman.dto.response.AddressResponse;
import com.ironman.dto.response.ApiResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    // Create new address
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody AddressRequest request) {

        log.info("Creating address for user: {}", currentUser.getId());
        AddressResponse address = addressService.createAddress(currentUser.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    // Get all addresses
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching addresses for user: {}", currentUser.getId());
        List<AddressResponse> addresses = addressService.getAllAddresses(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Addresses fetched successfully", addresses));
    }

    // Get single address by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Fetching address {} for user: {}", id, currentUser.getId());
        AddressResponse address = addressService.getAddressById(currentUser.getId(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Address fetched successfully", address));
    }

    // Update address
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {

        log.info("Updating address {} for user: {}", id, currentUser.getId());
        AddressResponse address = addressService.updateAddress(currentUser.getId(), id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Address updated successfully", address));
    }

    // Delete address
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteAddress(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Deleting address {} for user: {}", id, currentUser.getId());
        addressService.deleteAddress(currentUser.getId(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Address deleted successfully", null));
    }

    // Set default address
    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Setting default address {} for user: {}", id, currentUser.getId());
        AddressResponse address = addressService.setDefaultAddress(currentUser.getId(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Default address updated successfully", address));
    }
}