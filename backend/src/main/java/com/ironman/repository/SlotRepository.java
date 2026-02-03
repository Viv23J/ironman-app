package com.ironman.repository;

import com.ironman.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    Optional<Slot> findBySlotDateAndSlotTime(LocalDate slotDate, String slotTime);

    List<Slot> findBySlotDate(LocalDate slotDate);
}