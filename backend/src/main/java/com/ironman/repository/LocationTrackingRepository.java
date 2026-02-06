package com.ironman.repository;

import com.ironman.model.LocationTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationTrackingRepository extends JpaRepository<LocationTracking, Long> {

    List<LocationTracking> findByPartnerIdOrderByRecordedAtDesc(Long partnerId);

    List<LocationTracking> findByAssignmentIdOrderByRecordedAtDesc(Long assignmentId);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.partner.id = :partnerId " +
            "ORDER BY lt.recordedAt DESC LIMIT 1")
    Optional<LocationTracking> findLatestByPartnerId(@Param("partnerId") Long partnerId);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.assignment.id = :assignmentId " +
            "AND lt.recordedAt >= :since ORDER BY lt.recordedAt DESC")
    List<LocationTracking> findByAssignmentSince(@Param("assignmentId") Long assignmentId,
                                                 @Param("since") LocalDateTime since);
}