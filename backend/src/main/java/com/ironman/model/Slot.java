package com.ironman.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"slot_date", "slot_time"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false, length = 20)
    private String slotTime; // MORNING or EVENING

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity = 50;

    @Column(name = "current_bookings")
    private Integer currentBookings = 0;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}