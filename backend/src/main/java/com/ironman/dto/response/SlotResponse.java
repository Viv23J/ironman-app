package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {

    private LocalDate date;
    private String slotTime;       // MORNING or EVENING
    private String displayTime;    // "9:00 AM - 1:00 PM"
    private Integer maxCapacity;
    private Integer currentBookings;
    private Boolean isAvailable;
}