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
            String meetingLink
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
}
