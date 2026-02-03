package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.model.AddOn;
import com.ironman.model.ClothType;
import com.ironman.model.LaundryService;
import com.ironman.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final CatalogService catalogService;

    // --- Services ---

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<LaundryService>>> getAllServices(
            @RequestParam(required = false) String category) {

        List<LaundryService> services;
        if (category != null && !category.isEmpty()) {
            services = catalogService.getServicesByCategory(category);
        } else {
            services = catalogService.getAllServices();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Services fetched successfully", services));
    }

    // --- Cloth Types ---

    @GetMapping("/cloth-types")
    public ResponseEntity<ApiResponse<List<ClothType>>> getAllClothTypes(
            @RequestParam(required = false) String category) {

        List<ClothType> clothTypes;
        if (category != null && !category.isEmpty()) {
            clothTypes = catalogService.getClothTypesByCategory(category);
        } else {
            clothTypes = catalogService.getAllClothTypes();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Cloth types fetched successfully", clothTypes));
    }

    // --- Add-ons ---

    @GetMapping("/add-ons")
    public ResponseEntity<ApiResponse<List<AddOn>>> getAllAddOns() {

        List<AddOn> addOns = catalogService.getAllAddOns();

        return ResponseEntity.ok(
                ApiResponse.success("Add-ons fetched successfully", addOns));
    }
}