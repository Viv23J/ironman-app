package com.ironman.repository;

import com.ironman.model.DeliveryPartner;
import com.ironman.model.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long>, JpaSpecificationExecutor<DeliveryPartner> {
    Optional<DeliveryPartner> findByUserId(Long userId);

    List<DeliveryPartner> findByStatus(PartnerStatus status);

    List<DeliveryPartner> findByIsAvailableTrue();

    List<DeliveryPartner> findByStatusAndIsAvailableTrue(PartnerStatus status);

    boolean existsByUserId(Long userId);

    boolean existsByVehicleNumber(String vehicleNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT COUNT(p) FROM DeliveryPartner p WHERE p.status = 'APPROVED'")
    long countApprovedPartners();
}