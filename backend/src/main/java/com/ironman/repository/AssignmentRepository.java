package com.ironman.repository;

import com.ironman.model.Assignment;
import com.ironman.model.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByPartnerIdOrderByCreatedAtDesc(Long partnerId);

    List<Assignment> findByPartnerIdAndStatus(Long partnerId, AssignmentStatus status);

    List<Assignment> findByOrderId(Long orderId);

    Optional<Assignment> findByOrderIdAndAssignmentType(Long orderId, String assignmentType);

    List<Assignment> findByStatusOrderByCreatedAtDesc(AssignmentStatus status);

    Optional<Assignment> findByIdAndPartnerId(Long id, Long partnerId);
}