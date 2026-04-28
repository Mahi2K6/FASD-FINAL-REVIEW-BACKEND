package com.medconnect.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(
        name = "appointments",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_slot", columnNames = {"slot_id"})
        }
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    /** Populated when booking via slot-based flow; optional for legacy bookings. */
    @Column(name = "slot_id")
    private Long slotId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate appointmentDate;
    
    private String problemDescription;
    @Transient
    private String reason;

    @Column(length = 32)
    private String status; // "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
    private String meetingLink;

    // --- NEW SAAS FEATURES ---
    @JsonFormat(pattern = "HH:mm:ss")
    private java.time.LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private java.time.LocalTime endTime;

    @Transient
    private String date;

    @Transient
    private String time;

    @Column(length = 2000)
    private String callSummary; // Notes written by doctor after the call

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 16)
    private PaymentStatus paymentStatus;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    // --- CONSTRUCTORS ---

    public Appointment() {
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public java.time.LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(java.time.LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getProblemDescription() { return problemDescription != null ? problemDescription : reason; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public java.time.LocalTime getStartTime() { return startTime; }
    public void setStartTime(java.time.LocalTime startTime) { this.startTime = startTime; }

    public java.time.LocalTime getEndTime() { return endTime; }
    public void setEndTime(java.time.LocalTime endTime) { this.endTime = endTime; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCallSummary() { return callSummary; }
    public void setCallSummary(String callSummary) { this.callSummary = callSummary; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", slotId=" + slotId +
                ", appointmentDate=" + appointmentDate +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", status='" + status + '\'' +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}