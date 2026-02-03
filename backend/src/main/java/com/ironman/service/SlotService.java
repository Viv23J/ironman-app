package com.ironman.service;

import com.ironman.dto.response.SlotResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.model.Slot;
import com.ironman.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;

    private static final int DEFAULT_MAX_CAPACITY = 50;

    /**
     * Get available slots for a specific date
     * If slots don't exist in DB, create them dynamically
     */
    public List<SlotResponse> getAvailableSlots(LocalDate date) {
        log.info("Fetching available slots for date: {}", date);

        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book slots for past dates");
        }

        List<SlotResponse> slotResponses = new ArrayList<>();

        // Check MORNING slot
        Slot morningSlot = getOrCreateSlot(date, "MORNING");
        slotResponses.add(mapToResponse(morningSlot, date));

        // Check EVENING slot
        Slot eveningSlot = getOrCreateSlot(date, "EVENING");
        slotResponses.add(mapToResponse(eveningSlot, date));

        return slotResponses;
    }

    /**
     * Book a slot (increment current bookings)
     */
    @Transactional
    public void bookSlot(LocalDate date, String slotTime) {
        log.info("Booking slot: {} on {}", slotTime, date);

        Slot slot = getOrCreateSlot(date, slotTime);

        // Check if slot is full
        if (slot.getCurrentBookings() >= slot.getMaxCapacity()) {
            throw new BadRequestException("Slot is fully booked. Please choose another slot.");
        }

        // Check if slot is disabled
        if (!slot.getIsAvailable()) {
            throw new BadRequestException("This slot is not available. Please choose another slot.");
        }

        // Increment bookings
        slot.setCurrentBookings(slot.getCurrentBookings() + 1);
        slotRepository.save(slot);

        log.info("Slot booked successfully. Current bookings: {}", slot.getCurrentBookings());
    }

    /**
     * Cancel a slot booking (decrement current bookings)
     */
    @Transactional
    public void cancelSlot(LocalDate date, String slotTime) {
        log.info("Cancelling slot booking: {} on {}", slotTime, date);

        Slot slot = getOrCreateSlot(date, slotTime);

        if (slot.getCurrentBookings() > 0) {
            slot.setCurrentBookings(slot.getCurrentBookings() - 1);
            slotRepository.save(slot);
        }

        log.info("Slot booking cancelled successfully");
    }

    /**
     * Validate if a slot is available
     */
    public void validateSlot(LocalDate date, String slotTime) {
        // Validate slot time value
        if (!"MORNING".equals(slotTime) && !"EVENING".equals(slotTime)) {
            throw new BadRequestException("Invalid slot time. Must be MORNING or EVENING");
        }

        // Validate date
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book slots for past dates");
        }

        Slot slot = getOrCreateSlot(date, slotTime);

        if (!slot.getIsAvailable()) {
            throw new BadRequestException("Selected slot is not available");
        }

        if (slot.getCurrentBookings() >= slot.getMaxCapacity()) {
            throw new BadRequestException("Selected slot is fully booked");
        }
    }

    /**
     * Get or create a slot for a given date and time
     */
    private Slot getOrCreateSlot(LocalDate date, String slotTime) {
        return slotRepository.findBySlotDateAndSlotTime(date, slotTime)
                .orElseGet(() -> {
                    log.info("Creating new slot for {} - {}", date, slotTime);
                    Slot newSlot = new Slot();
                    newSlot.setSlotDate(date);
                    newSlot.setSlotTime(slotTime);
                    newSlot.setMaxCapacity(DEFAULT_MAX_CAPACITY);
                    newSlot.setCurrentBookings(0);
                    newSlot.setIsAvailable(true);
                    return slotRepository.save(newSlot);
                });
    }

    /**
     * Map Slot to SlotResponse
     */
    private SlotResponse mapToResponse(Slot slot, LocalDate date) {
        String displayTime = "MORNING".equals(slot.getSlotTime())
                ? "9:00 AM - 1:00 PM"
                : "3:00 PM - 7:00 PM";

        boolean isAvailable = slot.getIsAvailable()
                && slot.getCurrentBookings() < slot.getMaxCapacity();

        return SlotResponse.builder()
                .date(date)
                .slotTime(slot.getSlotTime())
                .displayTime(displayTime)
                .maxCapacity(slot.getMaxCapacity())
                .currentBookings(slot.getCurrentBookings())
                .isAvailable(isAvailable)
                .build();
    }
}