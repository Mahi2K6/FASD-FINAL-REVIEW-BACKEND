package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.HealthMetric;
import com.medconnect.backend.repository.HealthMetricRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    private final HealthMetricRepository healthMetricRepository;

    public MedicalRecordController(HealthMetricRepository healthMetricRepository) {
        this.healthMetricRepository = healthMetricRepository;
    }

    /**
     * Frontend currently calls /api/medical-records/{patientId}.
     * This returns all health metrics for a patient ordered by date.
     */
    @GetMapping("/{patientId}")
    public List<HealthMetric> getPatientMedicalRecords(@PathVariable Long patientId) {
        List<HealthMetric> rows = healthMetricRepository.findByPatientIdOrderByRecordedDateAsc(patientId);
        return rows == null ? List.of() : rows;
    }

    @GetMapping("/entry/{id}")
    public HealthMetric getMedicalRecordEntry(@PathVariable Long id) {
        return healthMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HealthMetric createMedicalRecord(@RequestBody HealthMetric record) {
        return healthMetricRepository.save(record);
    }

    @PutMapping("/entry/{id}")
    public HealthMetric updateMedicalRecord(@PathVariable Long id, @RequestBody HealthMetric payload) {
        HealthMetric existing = healthMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found: " + id));
        existing.setPatientId(payload.getPatientId());
        existing.setHeartRate(payload.getHeartRate());
        existing.setBloodPressure(payload.getBloodPressure());
        existing.setAdherenceScore(payload.getAdherenceScore());
        existing.setRecordedDate(payload.getRecordedDate());
        return healthMetricRepository.save(existing);
    }

    @DeleteMapping("/entry/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicalRecord(@PathVariable Long id) {
        if (!healthMetricRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medical record not found: " + id);
        }
        healthMetricRepository.deleteById(id);
    }
}
