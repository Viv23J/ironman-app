package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.AssignmentResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Assign pickup to partner (Admin/System)
     */
    @PostMapping("/pickup")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignPickup(
            @RequestParam Long orderId,
            @RequestParam Long partnerId) {

        log.info("Assigning pickup - Order: {}, Partner: {}", orderId, partnerId);
        AssignmentResponse assignment = assignmentService.assignPickup(orderId, partnerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pickup assigned successfully", assignment));
    }

    /**
     * Assign delivery to partner (Admin/System)
     */
    @PostMapping("/delivery")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignDelivery(
            @RequestParam Long orderId,
            @RequestParam Long partnerId) {

        log.info("Assigning delivery - Order: {}, Partner: {}", orderId, partnerId);
        AssignmentResponse assignment = assignmentService.assignDelivery(orderId, partnerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery assigned successfully", assignment));
    }

    /**
     * Partner accepts assignment
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<AssignmentResponse>> acceptAssignment(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Partner accepting assignment: {}", id);
        AssignmentResponse assignment = assignmentService.acceptAssignment(id, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Assignment accepted successfully", assignment));
    }

    /**
     * Partner rejects assignment
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AssignmentResponse>> rejectAssignment(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Partner rejecting assignment: {}", id);
        AssignmentResponse assignment = assignmentService.rejectAssignment(
                id, currentUser.getId(), reason);

        return ResponseEntity.ok(
                ApiResponse.success("Assignment rejected", assignment));
    }

    /**
     * Complete pickup
     */
    @PutMapping("/{id}/complete-pickup")
    public ResponseEntity<ApiResponse<AssignmentResponse>> completePickup(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Completing pickup for assignment: {}", id);
        AssignmentResponse assignment = assignmentService.completePickup(id, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Pickup completed successfully", assignment));
    }

    /**
     * Complete delivery
     */
    @PutMapping("/{id}/complete-delivery")
    public ResponseEntity<ApiResponse<AssignmentResponse>> completeDelivery(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Completing delivery for assignment: {}", id);
        AssignmentResponse assignment = assignmentService.completeDelivery(id, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Delivery completed successfully", assignment));
    }

    /**
     * Get my assignments (for partners)
     */
    @GetMapping("/my-assignments")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignments(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(required = false) String status) {

        log.info("Fetching assignments for user: {}", currentUser.getId());
        List<AssignmentResponse> assignments = assignmentService.getPartnerAssignments(
                currentUser.getId(), status);

        return ResponseEntity.ok(
                ApiResponse.success("Assignments fetched successfully", assignments));
    }

    /**
     * Get assignments for an order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getOrderAssignments(
            @PathVariable Long orderId) {

        log.info("Fetching assignments for order: {}", orderId);
        List<AssignmentResponse> assignments = assignmentService.getOrderAssignments(orderId);

        return ResponseEntity.ok(
                ApiResponse.success("Order assignments fetched successfully", assignments));
    }
}