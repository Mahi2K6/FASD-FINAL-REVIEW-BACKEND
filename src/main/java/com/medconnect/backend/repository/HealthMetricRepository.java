package com.medconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medconnect.backend.model.HealthMetric;
import java.util.List;

public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    // Fetches patient data ordered by date for the Chart.js graphs
    List<HealthMetric> findByPatientIdOrderByRecordedDateAsc(Long patientId);
}