package com.ironman.controller;

import com.ironman.dto.request.OrderSearchRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.OrderResponse;
import com.ironman.dto.response.PartnerResponse;
import com.ironman.dto.response.UserManagementResponse;
import com.ironman.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    /**
     * Advanced order search
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> searchOrders(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Searching orders with filters");

        OrderSearchRequest request = new OrderSearchRequest();
        request.setOrderNumber(orderNumber);
        request.setStatus(status);
        request.setPaymentStatus(paymentStatus);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setCustomerPhone(customerPhone);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        Page<OrderResponse> orders = searchService.searchOrders(request);

        return ResponseEntity.ok(
                ApiResponse.success("Orders found: " + orders.getTotalElements(), orders));
    }

    /**
     * Search users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> searchUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate registeredAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate registeredBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching users with term: {}", searchTerm);

        Page<UserManagementResponse> users = searchService.searchUsers(
                searchTerm, role, registeredAfter, registeredBefore, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Users found: " + users.getTotalElements(), users));
    }

    /**
     * Search delivery partners
     */
    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<Page<PartnerResponse>>> searchPartners(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching partners with term: {}", searchTerm);

        Page<PartnerResponse> partners = searchService.searchPartners(
                searchTerm, status, isAvailable, vehicleType, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Partners found: " + partners.getTotalElements(), partners));
    }

    /**
     * Get available partners (quick access)
     */
    @GetMapping("/partners/available")
    public ResponseEntity<ApiResponse<List<PartnerResponse>>> getAvailablePartners() {

        log.info("Fetching available partners");
        List<PartnerResponse> partners = searchService.getAvailablePartners();

        return ResponseEntity.ok(
                ApiResponse.success("Available partners: " + partners.size(), partners));
    }
}