package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.SlotResponse;
import com.ironman.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
@RequiredArgsConstructor
@Slf4j
public class SlotController {

    private final SlotService slotService;

    // Get available slots for a date
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getAvailableSlots(
            @RequestParam String date) {

        log.info("Fetching available slots for date: {}", date);
        LocalDate slotDate = LocalDate.parse(date);
        List<SlotResponse> slots = slotService.getAvailableSlots(slotDate);

        return ResponseEntity.ok(
                ApiResponse.success("Slots fetched successfully", slots));
    }
}