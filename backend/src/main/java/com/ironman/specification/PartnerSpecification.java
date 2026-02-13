package com.ironman.specification;

import com.ironman.model.DeliveryPartner;
import com.ironman.model.PartnerStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PartnerSpecification {

    /**
     * Search partners with multiple criteria
     */
    public static Specification<DeliveryPartner> searchPartners(
            String searchTerm,
            PartnerStatus status,
            Boolean isAvailable,
            String vehicleType) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by partner name, phone, or vehicle number
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("fullName")), searchPattern
                );
                Predicate phonePredicate = criteriaBuilder.like(
                        root.get("user").get("phone"), searchPattern
                );
                Predicate vehiclePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("vehicleNumber")), searchPattern
                );

                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate, vehiclePredicate));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by availability
            if (isAvailable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
            }

            // Filter by vehicle type
            if (vehicleType != null && !vehicleType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("vehicleType")),
                        vehicleType.toLowerCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Available partners only
     */
    public static Specification<DeliveryPartner> isAvailable() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("status"), PartnerStatus.APPROVED),
                        criteriaBuilder.equal(root.get("isAvailable"), true)
                );
    }
}