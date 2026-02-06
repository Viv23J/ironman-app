package com.ironman.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType; // BIKE, SCOOTER, CAR

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "bank_account_number", length = 20)
    private String bankAccountNumber;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "license_image_url")
    private String licenseImageUrl;

    @Column(name = "aadhar_image_url")
    private String aadharImageUrl;

    @Column(name = "vehicle_rc_url")
    private String vehicleRcUrl;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerStatus status = PartnerStatus.PENDING_APPROVAL;

    @Column(name = "is_available")
    private Boolean isAvailable = false;

    @Column(name = "current_latitude", precision = 10, scale = 8)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 11, scale = 8)
    private BigDecimal currentLongitude;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}