package com.medconnect.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "dark_mode")
    private boolean darkMode = false;

    @Column(name = "email_notifications")
    private boolean emailNotifications = true;

    @Column(name = "appointment_reminders")
    private boolean appointmentReminders = true;

    @Column(name = "pharmacy_updates")
    private boolean pharmacyUpdates = true;

    @Column(name = "prescription_updates")
    private boolean prescriptionUpdates = true;

    @Column(name = "privacy_phone_visible")
    private boolean privacyPhoneVisible = false;

    @Column(name = "share_medical_records")
    private boolean shareMedicalRecords = false;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isAppointmentReminders() { return appointmentReminders; }
    public void setAppointmentReminders(boolean appointmentReminders) { this.appointmentReminders = appointmentReminders; }

    public boolean isPharmacyUpdates() { return pharmacyUpdates; }
    public void setPharmacyUpdates(boolean pharmacyUpdates) { this.pharmacyUpdates = pharmacyUpdates; }

    public boolean isPrescriptionUpdates() { return prescriptionUpdates; }
    public void setPrescriptionUpdates(boolean prescriptionUpdates) { this.prescriptionUpdates = prescriptionUpdates; }

    public boolean isPrivacyPhoneVisible() { return privacyPhoneVisible; }
    public void setPrivacyPhoneVisible(boolean privacyPhoneVisible) { this.privacyPhoneVisible = privacyPhoneVisible; }

    public boolean isShareMedicalRecords() { return shareMedicalRecords; }
    public void setShareMedicalRecords(boolean shareMedicalRecords) { this.shareMedicalRecords = shareMedicalRecords; }
}
