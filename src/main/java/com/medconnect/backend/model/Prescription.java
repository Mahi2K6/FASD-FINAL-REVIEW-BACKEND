package com.medconnect.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @Column(length = 500)
    private String symptoms;

    @Column(name = "consultation_status", length = 32)
    private String consultationStatus;

    @Column(name = "prescription_status", length = 32)
    private String prescriptionStatus;

    @Column(length = 500)
    private String followUpRecommendation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prescription")
    private java.util.List<PrescriptionMedicine> medicines = new java.util.ArrayList<>();

    private String status; // Kept for legacy compatibility, typically ACTIVE/COMPLETED

    @Column(length = 500)
    private String diagnosis;

    @Column(length = 2000)
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (prescriptionStatus == null) {
            prescriptionStatus = "ACTIVE";
        }
        if (consultationStatus == null) {
            consultationStatus = "COMPLETED";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public java.util.List<PrescriptionMedicine> getMedicines() { return medicines; }
    public void setMedicines(java.util.List<PrescriptionMedicine> medicines) { this.medicines = medicines; }
    public String getFollowUpRecommendation() { return followUpRecommendation; }
    public void setFollowUpRecommendation(String followUpRecommendation) { this.followUpRecommendation = followUpRecommendation; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getConsultationStatus() { return consultationStatus; }
    public void setConsultationStatus(String consultationStatus) { this.consultationStatus = consultationStatus; }
    public String getPrescriptionStatus() { return prescriptionStatus; }
    public void setPrescriptionStatus(String prescriptionStatus) { this.prescriptionStatus = prescriptionStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}