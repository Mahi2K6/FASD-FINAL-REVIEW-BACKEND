package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.HealthMetric;
import com.medconnect.backend.repository.HealthMetricRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
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
    public ResponseEntity<List<HealthMetric>> getPatientMedicalRecords(@PathVariable Long patientId) {
        List<HealthMetric> rows = healthMetricRepository.findByPatientIdOrderByRecordedDateAsc(patientId);
        return ResponseEntity.ok(rows == null ? List.of() : rows);
    }

    @GetMapping("/entry/{id}")
    public ResponseEntity<HealthMetric> getMedicalRecordEntry(@PathVariable Long id) {
        return healthMetricRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HealthMetric> createMedicalRecord(@RequestBody HealthMetric record) {
        HealthMetric saved = healthMetricRepository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/entry/{id}")
    public ResponseEntity<HealthMetric> updateMedicalRecord(@PathVariable Long id, @RequestBody HealthMetric payload) {
        return healthMetricRepository.findById(id)
                .map(existing -> {
                    existing.setPatientId(payload.getPatientId());
                    existing.setHeartRate(payload.getHeartRate());
                    existing.setBloodPressure(payload.getBloodPressure());
                    existing.setAdherenceScore(payload.getAdherenceScore());
                    existing.setRecordedDate(payload.getRecordedDate());
                    HealthMetric updated = healthMetricRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/entry/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long id) {
        if (healthMetricRepository.existsById(id)) {
            healthMetricRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}