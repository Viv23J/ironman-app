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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final LaundryServiceRepository laundryServiceRepository;
    private final ClothTypeRepository clothTypeRepository;
    private final AddOnRepository addOnRepository;

    // --- Laundry Services ---

    public List<LaundryService> getAllServices() {
        log.info("Fetching all active services");
        return laundryServiceRepository.findByIsActiveTrue();
    }

    public List<LaundryService> getServicesByCategory(String category) {
        log.info("Fetching services by category: {}", category);
        return laundryServiceRepository.findByCategoryAndIsActiveTrue(category);
    }

    // --- Cloth Types ---

    public List<ClothType> getAllClothTypes() {
        log.info("Fetching all active cloth types");
        return clothTypeRepository.findByIsActiveTrue();
    }

    public List<ClothType> getClothTypesByCategory(String category) {
        log.info("Fetching cloth types by category: {}", category);
        return clothTypeRepository.findByCategoryAndIsActiveTrue(category);
    }

    // --- Add-ons ---

    public List<AddOn> getAllAddOns() {
        log.info("Fetching all active add-ons");
        return addOnRepository.findByIsActiveTrue();
    }
}