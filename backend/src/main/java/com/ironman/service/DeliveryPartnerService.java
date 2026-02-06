package com.ironman.service;

import com.ironman.dto.request.LocationUpdateRequest;
import com.ironman.dto.request.PartnerRegistrationRequest;
import com.ironman.dto.response.LocationResponse;
import com.ironman.dto.response.PartnerResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.DeliveryPartnerRepository;
import com.ironman.repository.LocationTrackingRepository;
import com.ironman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final LocationTrackingRepository locationTrackingRepository;

    /**
     * Register as a delivery partner
     */
    @Transactional
    public PartnerResponse registerPartner(Long userId, PartnerRegistrationRequest request) {
        log.info("Registering delivery partner for user: {}", userId);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is already a partner
        if (partnerRepository.existsByUserId(userId)) {
            throw new BadRequestException("User is already registered as a delivery partner");
        }

        // Check for duplicate vehicle number
        if (partnerRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new BadRequestException("Vehicle number already registered");
        }

        // Check for duplicate license number
        if (partnerRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BadRequestException("License number already registered");
        }

        // Update user role to DELIVERY_PARTNER
        user.setRole(UserRole.DELIVERY_PARTNER);
        userRepository.save(user);

        // Create partner profile
        DeliveryPartner partner = new DeliveryPartner();
        partner.setUser(user);
        partner.setVehicleType(request.getVehicleType());
        partner.setVehicleNumber(request.getVehicleNumber());
        partner.setLicenseNumber(request.getLicenseNumber());
        partner.setAadharNumber(request.getAadharNumber());
        partner.setPanNumber(request.getPanNumber());
        partner.setBankAccountNumber(request.getBankAccountNumber());
        partner.setIfscCode(request.getIfscCode());
        partner.setStatus(PartnerStatus.PENDING_APPROVAL);
        partner.setIsAvailable(false);

        DeliveryPartner savedPartner = partnerRepository.save(partner);
        log.info("Delivery partner registered successfully: {}", savedPartner.getId());

        return mapToPartnerResponse(savedPartner);
    }

    /**
     * Get partner profile by user ID
     */
    public PartnerResponse getPartnerProfile(Long userId) {
        log.info("Fetching partner profile for user: {}", userId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        return mapToPartnerResponse(partner);
    }

    /**
     * Get partner by ID
     */
    public PartnerResponse getPartnerById(Long partnerId) {
        log.info("Fetching partner: {}", partnerId);

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        return mapToPartnerResponse(partner);
    }

    /**
     * Toggle partner availability
     */
    @Transactional
    public PartnerResponse toggleAvailability(Long userId) {
        log.info("Toggling availability for user: {}", userId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        // Only approved partners can toggle availability
        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new BadRequestException("Only approved partners can toggle availability. Current status: " + partner.getStatus());
        }

        partner.setIsAvailable(!partner.getIsAvailable());
        DeliveryPartner updated = partnerRepository.save(partner);

        log.info("Partner availability updated to: {}", updated.getIsAvailable());
        return mapToPartnerResponse(updated);
    }

    /**
     * Update partner location
     */
    @Transactional
    public LocationResponse updateLocation(Long userId, LocationUpdateRequest request) {
        log.info("Updating location for user: {}", userId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        // Update current location on partner
        partner.setCurrentLatitude(request.getLatitude());
        partner.setCurrentLongitude(request.getLongitude());
        partnerRepository.save(partner);

        // Save location tracking history
        LocationTracking tracking = new LocationTracking();
        tracking.setPartner(partner);
        tracking.setLatitude(request.getLatitude());
        tracking.setLongitude(request.getLongitude());
        tracking.setAccuracy(request.getAccuracy());

        // Link to assignment if provided
        if (request.getAssignmentId() != null) {
            Assignment assignment = new Assignment();
            assignment.setId(request.getAssignmentId());
            tracking.setAssignment(assignment);
        }

        LocationTracking saved = locationTrackingRepository.save(tracking);

        log.info("Location updated successfully");
        return mapToLocationResponse(saved);
    }

    /**
     * Get available partners (for assignment)
     */
    public List<PartnerResponse> getAvailablePartners() {
        log.info("Fetching available partners");

        List<DeliveryPartner> partners = partnerRepository
                .findByStatusAndIsAvailableTrue(PartnerStatus.APPROVED);

        return partners.stream()
                .map(this::mapToPartnerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending approval partners (Admin only)
     */
    public List<PartnerResponse> getPendingPartners() {
        log.info("Fetching pending approval partners");

        List<DeliveryPartner> partners = partnerRepository.findByStatus(PartnerStatus.PENDING_APPROVAL);

        return partners.stream()
                .map(this::mapToPartnerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve partner (Admin only)
     */
    @Transactional
    public PartnerResponse approvePartner(Long partnerId, String notes) {
        log.info("Approving partner: {}", partnerId);

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (partner.getStatus() != PartnerStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Partner is not in PENDING_APPROVAL status");
        }

        partner.setStatus(PartnerStatus.APPROVED);
        partner.setApprovalNotes(notes);
        DeliveryPartner updated = partnerRepository.save(partner);

        log.info("Partner approved successfully");
        return mapToPartnerResponse(updated);
    }

    /**
     * Reject partner (Admin only)
     */
    @Transactional
    public PartnerResponse rejectPartner(Long partnerId, String reason) {
        log.info("Rejecting partner: {}", partnerId);

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        partner.setStatus(PartnerStatus.REJECTED);
        partner.setApprovalNotes(reason);
        DeliveryPartner updated = partnerRepository.save(partner);

        log.info("Partner rejected");
        return mapToPartnerResponse(updated);
    }

    /**
     * Get partner's location history
     */
    public List<LocationResponse> getLocationHistory(Long partnerId) {
        log.info("Fetching location history for partner: {}", partnerId);

        List<LocationTracking> locations = locationTrackingRepository
                .findByPartnerIdOrderByRecordedAtDesc(partnerId);

        return locations.stream()
                .map(this::mapToLocationResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

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

    private LocationResponse mapToLocationResponse(LocationTracking tracking) {
        return LocationResponse.builder()
                .id(tracking.getId())
                .partnerId(tracking.getPartner().getId())
                .assignmentId(tracking.getAssignment() != null ? tracking.getAssignment().getId() : null)
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .accuracy(tracking.getAccuracy())
                .recordedAt(tracking.getRecordedAt())
                .build();
    }
}