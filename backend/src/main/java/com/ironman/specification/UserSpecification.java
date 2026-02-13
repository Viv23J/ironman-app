package com.ironman.specification;

import com.ironman.model.User;
import com.ironman.model.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    /**
     * Search users with multiple criteria
     */
    public static Specification<User> searchUsers(
            String searchTerm,
            UserRole role,
            LocalDateTime registeredAfter,
            LocalDateTime registeredBefore) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name, phone, or email
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")), searchPattern
                );
                Predicate phonePredicate = criteriaBuilder.like(
                        root.get("phone"), searchPattern
                );
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern
                );

                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate, emailPredicate));
            }

            // Filter by role
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            // Filter by registration date
            if (registeredAfter != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), registeredAfter
                ));
            }

            if (registeredBefore != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), registeredBefore
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Search users by role
     */
    public static Specification<User> byRole(UserRole role) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role"), role);
    }

    /**
     * Search users registered after date
     */
    public static Specification<User> registeredAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }
}