package com.ironman.service;

import com.ironman.model.AddOn;
import com.ironman.model.ClothType;
import com.ironman.model.LaundryService;
import com.ironman.repository.AddOnRepository;
import com.ironman.repository.ClothTypeRepository;
import com.ironman.repository.LaundryServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final LaundryServiceRepository laundryServiceRepository;
    private final ClothTypeRepository clothTypeRepository;
    private final AddOnRepository addOnRepository;

    // --- Laundry Services ---

    @Cacheable(value = "services", key = "'all'")
    public List<LaundryService> getAllServices() {
        log.info("Fetching all services from database");


        return laundryServiceRepository.findAll();
    }

    public List<LaundryService> getServicesByCategory(String category) {
        log.info("Fetching services by category: {}", category);
        return laundryServiceRepository.findByCategoryAndIsActiveTrue(category);
    }

    // --- Cloth Types ---

    @Cacheable(value = "clothTypes", key = "'all'")
    public List<ClothType> getAllClothTypes() {
        log.info("Fetching all cloth types from database");

        return clothTypeRepository.findAll();
    }

    public List<ClothType> getClothTypesByCategory(String category) {
        log.info("Fetching cloth types by category: {}", category);
        return clothTypeRepository.findByCategoryAndIsActiveTrue(category);
    }

    // --- Add-ons ---
    @Cacheable(value = "addons", key = "'all'")
    public List<AddOn> getAllAddOns() {
        log.info("Fetching all active add-ons");
        return addOnRepository.findByIsActiveTrue();
    }
}