package com.medconnect.backend.controller;

import com.medconnect.backend.model.dto.SlotResponseDTO;
import com.medconnect.backend.service.DoctorAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    public SlotController(DoctorAvailabilityService doctorAvailabilityService) {
        this.doctorAvailabilityService = doctorAvailabilityService;
    }

    /**
     * Returns available (not booked, not expired) 30-minute slots for the doctor on the given date.
     * Ensures the day's slot grid exists (10:00–18:00).
     */
    @GetMapping("/{doctorId}")
    public List<SlotResponseDTO> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return doctorAvailabilityService.getAvailableSlots(doctorId, date);
    }
}
