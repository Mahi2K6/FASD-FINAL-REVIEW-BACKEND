package com.medconnect.backend.model.dto;

public class AppointmentResponseDTO {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private String status;
    private String problemDescription;
    private String paymentStatus;
    private String meetingLink;
    /** Set when booking uses a slot; null for legacy rows. */
    private Long slotId;
    /** Populated for doctor-facing responses; null for patient /my list. */
    private String patientName;
    /** Populated for doctor-facing responses; null for patient /my list. */
    private String patientPhone;

    public AppointmentResponseDTO(
            Long id,
            Long patientId,
            Long doctorId,
            String doctorName,
            String doctorSpecialty,
            String appointmentDate,
            String startTime,
            String endTime,
            String status,
            String problemDescription,
            String paymentStatus,
            String meetingLink,
            Long slotId,
            String patientName,
            String patientPhone
    ) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.problemDescription = problemDescription;
        this.paymentStatus = paymentStatus;
        this.meetingLink = meetingLink;
        this.slotId = slotId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public String getProblemDescription() { return problemDescription; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getMeetingLink() { return meetingLink; }
    public Long getSlotId() { return slotId; }
    public String getPatientName() { return patientName; }
    public String getPatientPhone() { return patientPhone; }
}
