package com.medconnect.backend.model.dto;

import java.util.Date;

public class AppointmentResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Date appointmentDate;
    private String startTime;
    private String endTime;
    private String status;

    public AppointmentResponse(
            Long id,
            Long doctorId,
            String doctorName,
            String doctorSpecialization,
            Date appointmentDate,
            String startTime,
            String endTime,
            String status
    ) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialization() { return doctorSpecialization; }
    public Date getAppointmentDate() { return appointmentDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
}
