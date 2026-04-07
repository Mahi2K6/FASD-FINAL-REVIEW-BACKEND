package com.medconnect.backend.service;

import com.medconnect.backend.model.dto.SlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface DoctorAvailabilityService {

    void generateSlots(Long doctorId, LocalDate date);

    List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date);
}
