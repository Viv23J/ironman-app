package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.PartnerResponse;
import com.ironman.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final DeliveryPartnerService partnerService;

    /**
     * Get all pending approval partners
     */
    @GetMapping("/partners/pending")
    public ResponseEntity<ApiResponse<List<PartnerResponse>>> getPendingPartners() {

        log.info("Fetching pending approval partners");
        List<PartnerResponse> partners = partnerService.getPendingPartners();

        return ResponseEntity.ok(
                ApiResponse.success("Pending partners fetched successfully", partners));
    }

    /**
     * Approve partner
     */
    @PutMapping("/partners/{partnerId}/approve")
    public ResponseEntity<ApiResponse<PartnerResponse>> approvePartner(
            @PathVariable Long partnerId,
            @RequestParam(required = false) String notes) {

        log.info("Approving partner: {}", partnerId);
        PartnerResponse partner = partnerService.approvePartner(partnerId, notes);

        return ResponseEntity.ok(
                ApiResponse.success("Partner approved successfully", partner));
    }

    /**
     * Reject partner
     */
    @PutMapping("/partners/{partnerId}/reject")
    public ResponseEntity<ApiResponse<PartnerResponse>> rejectPartner(
            @PathVariable Long partnerId,
            @RequestParam String reason) {

        log.info("Rejecting partner: {}", partnerId);
        PartnerResponse partner = partnerService.rejectPartner(partnerId, reason);

        return ResponseEntity.ok(
                ApiResponse.success("Partner rejected", partner));
    }

    /**
     * Get partner details by ID
     */
    @GetMapping("/partners/{partnerId}")
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartnerById(
            @PathVariable Long partnerId) {

        log.info("Fetching partner: {}", partnerId);
        PartnerResponse partner = partnerService.getPartnerById(partnerId);

        return ResponseEntity.ok(
                ApiResponse.success("Partner fetched successfully", partner));
    }
}
