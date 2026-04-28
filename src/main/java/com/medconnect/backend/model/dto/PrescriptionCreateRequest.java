package com.medconnect.backend.model.dto;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionCreateRequest {

    private Long appointmentId;
    private Long doctorId;
    private Long patientId;
    private String diagnosis;
    private String notes;
    private String medicines;
    private List<MedicineEntry> medicineList = new ArrayList<>();

    public static class MedicineEntry {
        private String medicineName;
        private String dosage;
        private String frequency;
        private String duration;
        private Integer quantity;
        private java.math.BigDecimal estimatedCost;

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public java.math.BigDecimal getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(java.math.BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    }

    // --- Getters and Setters ---

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getMedicines() { return medicines; }
    public void setMedicines(String medicines) { this.medicines = medicines; }

    public List<MedicineEntry> getMedicineList() { return medicineList; }
    public void setMedicineList(List<MedicineEntry> medicineList) { this.medicineList = medicineList; }
}
