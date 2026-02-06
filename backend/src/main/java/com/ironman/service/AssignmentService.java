package com.ironman.service;

import com.ironman.dto.response.AssignmentResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.*;
import com.ironman.repository.AssignmentRepository;
import com.ironman.repository.DeliveryPartnerRepository;
import com.ironman.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final DeliveryPartnerRepository partnerRepository;

    /**
     * Assign order to partner for pickup
     */
    @Transactional
    public AssignmentResponse assignPickup(Long orderId, Long partnerId) {
        log.info("Assigning pickup for order {} to partner {}", orderId, partnerId);

        // Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check order status
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PICKUP_ASSIGNED) {
            throw new BadRequestException("Order is not in PENDING status. Current: " + order.getStatus());
        }

        // Validate partner
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        // Check partner is approved and available
        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new BadRequestException("Partner is not approved. Status: " + partner.getStatus());
        }

        if (!partner.getIsAvailable()) {
            throw new BadRequestException("Partner is not available");
        }

        // Create assignment
        Assignment assignment = new Assignment();
        assignment.setOrder(order);
        assignment.setPartner(partner);
        assignment.setAssignmentType("PICKUP");
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(assignment);

        // Update order status
        order.setStatus(OrderStatus.PICKUP_ASSIGNED);
        orderRepository.save(order);

        log.info("Pickup assignment created: {}", saved.getId());
        return mapToAssignmentResponse(saved);
    }

    /**
     * Assign order to partner for delivery
     */
    @Transactional
    public AssignmentResponse assignDelivery(Long orderId, Long partnerId) {
        log.info("Assigning delivery for order {} to partner {}", orderId, partnerId);

        // Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check order is ready for delivery
        if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
            throw new BadRequestException("Order is not ready for delivery. Current: " + order.getStatus());
        }

        // Validate partner
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (partner.getStatus() != PartnerStatus.APPROVED) {
            throw new BadRequestException("Partner is not approved");
        }

        if (!partner.getIsAvailable()) {
            throw new BadRequestException("Partner is not available");
        }

        // Create assignment
        Assignment assignment = new Assignment();
        assignment.setOrder(order);
        assignment.setPartner(partner);
        assignment.setAssignmentType("DELIVERY");
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(assignment);

        // Update order status
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);

        log.info("Delivery assignment created: {}", saved.getId());
        return mapToAssignmentResponse(saved);
    }

    /**
     * Partner accepts assignment
     */
    @Transactional
    public AssignmentResponse acceptAssignment(Long assignmentId, Long userId) {
        log.info("Partner accepting assignment: {}", assignmentId);

        // Get partner profile
        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        // Get assignment
        Assignment assignment = assignmentRepository.findByIdAndPartnerId(assignmentId, partner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found or not assigned to you"));

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new BadRequestException("Assignment cannot be accepted. Current status: " + assignment.getStatus());
        }

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(LocalDateTime.now());
        Assignment updated = assignmentRepository.save(assignment);

        log.info("Assignment accepted by partner");
        return mapToAssignmentResponse(updated);
    }

    /**
     * Partner rejects assignment
     */
    @Transactional
    public AssignmentResponse rejectAssignment(Long assignmentId, Long userId, String reason) {
        log.info("Partner rejecting assignment: {}", assignmentId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        Assignment assignment = assignmentRepository.findByIdAndPartnerId(assignmentId, partner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new BadRequestException("Assignment cannot be rejected. Current status: " + assignment.getStatus());
        }

        assignment.setStatus(AssignmentStatus.REJECTED);
        assignment.setNotes(reason);
        Assignment updated = assignmentRepository.save(assignment);

        // Reset order status
        Order order = assignment.getOrder();
        if ("PICKUP".equals(assignment.getAssignmentType())) {
            order.setStatus(OrderStatus.PENDING);
        } else {
            order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        }
        orderRepository.save(order);

        log.info("Assignment rejected by partner");
        return mapToAssignmentResponse(updated);
    }

    /**
     * Mark pickup as completed
     */
    @Transactional
    public AssignmentResponse completePickup(Long assignmentId, Long userId) {
        log.info("Completing pickup for assignment: {}", assignmentId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        Assignment assignment = assignmentRepository.findByIdAndPartnerId(assignmentId, partner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!"PICKUP".equals(assignment.getAssignmentType())) {
            throw new BadRequestException("This is not a pickup assignment");
        }

        if (assignment.getStatus() != AssignmentStatus.ACCEPTED) {
            throw new BadRequestException("Assignment must be accepted first");
        }

        assignment.setStatus(AssignmentStatus.PICKED_UP);
        assignment.setCompletedAt(LocalDateTime.now());
        Assignment updated = assignmentRepository.save(assignment);

        // Update order
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.PICKED_UP);
        order.setActualPickupTime(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Pickup completed successfully");
        return mapToAssignmentResponse(updated);
    }

    /**
     * Mark delivery as completed
     */
    @Transactional
    public AssignmentResponse completeDelivery(Long assignmentId, Long userId) {
        log.info("Completing delivery for assignment: {}", assignmentId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        Assignment assignment = assignmentRepository.findByIdAndPartnerId(assignmentId, partner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!"DELIVERY".equals(assignment.getAssignmentType())) {
            throw new BadRequestException("This is not a delivery assignment");
        }

        if (assignment.getStatus() != AssignmentStatus.ACCEPTED) {
            throw new BadRequestException("Assignment must be accepted first");
        }

        assignment.setStatus(AssignmentStatus.DELIVERED);
        assignment.setCompletedAt(LocalDateTime.now());
        Assignment updated = assignmentRepository.save(assignment);

        // Update order
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setActualDeliveryTime(LocalDateTime.now());
        orderRepository.save(order);

        // Increment partner's delivery count
        partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
        partnerRepository.save(partner);

        log.info("Delivery completed successfully");
        return mapToAssignmentResponse(updated);
    }

    /**
     * Get all assignments for a partner
     */
    public List<AssignmentResponse> getPartnerAssignments(Long userId, String status) {
        log.info("Fetching assignments for partner user: {}", userId);

        DeliveryPartner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        List<Assignment> assignments;
        if (status != null && !status.isEmpty()) {
            assignments = assignmentRepository.findByPartnerIdAndStatus(
                    partner.getId(), AssignmentStatus.valueOf(status));
        } else {
            assignments = assignmentRepository.findByPartnerIdOrderByCreatedAtDesc(partner.getId());
        }

        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get assignments for an order
     */
    public List<AssignmentResponse> getOrderAssignments(Long orderId) {
        log.info("Fetching assignments for order: {}", orderId);

        List<Assignment> assignments = assignmentRepository.findByOrderId(orderId);

        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        Order order = assignment.getOrder();
        DeliveryPartner partner = assignment.getPartner();

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .partnerId(partner.getId())
                .partnerName(partner.getUser().getFullName())
                .assignmentType(assignment.getAssignmentType())
                .status(assignment.getStatus().name())
                .pickupAddress(formatAddress(order.getPickupAddress()))
                .deliveryAddress(formatAddress(order.getDeliveryAddress()))
                .customerPhone(order.getCustomer().getPhone())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .completedAt(assignment.getCompletedAt())
                .notes(assignment.getNotes())
                .createdAt(assignment.getCreatedAt())
                .build();
    }

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