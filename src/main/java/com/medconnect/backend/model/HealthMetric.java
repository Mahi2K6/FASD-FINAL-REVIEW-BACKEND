package com.medconnect.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "health_metrics")
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private int heartRate;        // e.g., 72
    private String bloodPressure; // e.g., "120/80"
    private int adherenceScore;   // e.g., 95 (percentage)
    private LocalDate recordedDate = LocalDate.now();

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }
    
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    
    public int getAdherenceScore() { return adherenceScore; }
    public void setAdherenceScore(int adherenceScore) { this.adherenceScore = adherenceScore; }
    
    public LocalDate getRecordedDate() { return recordedDate; }
    public void setRecordedDate(LocalDate recordedDate) { this.recordedDate = recordedDate; }
}