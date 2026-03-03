package com.medconnect.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private Long doctorId;
    private Date appointmentDate;
    
    private String problemDescription;
    private String status; // "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
    private String meetingLink;

    // --- NEW SAAS FEATURES ---
    private String startTime; // e.g., "10:30"
    private String endTime;   // e.g., "11:00"

    @Column(length = 2000)
    private String callSummary; // Notes written by doctor after the call

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getCallSummary() { return callSummary; }
    public void setCallSummary(String callSummary) { this.callSummary = callSummary; }
}