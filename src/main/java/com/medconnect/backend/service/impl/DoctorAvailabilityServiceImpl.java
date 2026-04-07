package com.medconnect.backend.service.impl;

import com.medconnect.backend.model.DoctorAvailability;
import com.medconnect.backend.model.dto.SlotResponse;
import com.medconnect.backend.repository.DoctorAvailabilityRepository;
import com.medconnect.backend.service.DoctorAvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {
    private static final Logger log = LoggerFactory.getLogger(DoctorAvailabilityServiceImpl.class);

    private static final LocalTime WORK_DAY_START = LocalTime.of(10, 0);
    private static final LocalTime WORK_DAY_END = LocalTime.of(18, 0);
    private static final int SLOT_MINUTES = 30;

    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    public DoctorAvailabilityServiceImpl(DoctorAvailabilityRepository doctorAvailabilityRepository) {
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
    }

    @Override
    @Transactional
    public void generateSlots(Long doctorId, LocalDate date) {
        LocalTime cursor = WORK_DAY_START;
        while (cursor.isBefore(WORK_DAY_END)) {
            LocalTime slotEnd = cursor.plusMinutes(SLOT_MINUTES);
            if (slotEnd.isAfter(WORK_DAY_END)) {
                break;
            }
            if (!doctorAvailabilityRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, date, cursor)) {
                DoctorAvailability slot = new DoctorAvailability();
                slot.setDoctorId(doctorId);
                slot.setSlotDate(date);
                slot.setStartTime(cursor);
                slot.setEndTime(slotEnd);
                slot.setBooked(false);
                doctorAvailabilityRepository.save(slot);
            }
            cursor = slotEnd;
        }
    }

    @Override
    @Transactional
    public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone);

        if (date.isBefore(today)) {
            log.info("Slots returned: {}", 0);
            return Collections.emptyList();
        }

        generateSlots(doctorId, date);

        List<DoctorAvailability> rows = doctorAvailabilityRepository.findAvailableSlots(doctorId, date);
        if (date.equals(today)) {
            rows = rows.stream()
                    .filter(slot -> slot.getStartTime().isAfter(now))
                    .toList();
        }

        List<SlotResponse> result = new ArrayList<>();
        for (DoctorAvailability s : rows) {
            result.add(new SlotResponse(
                    s.getId(),
                    s.getDoctorId(),
                    s.getSlotDate(),
                    s.getStartTime(),
                    s.getEndTime(),
                    s.isBooked()
            ));
        }
        log.info("Slots returned: {}", result.size());
        return result;
    }
}
