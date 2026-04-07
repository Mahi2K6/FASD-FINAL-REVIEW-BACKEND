package com.medconnect.backend.service;

import com.medconnect.backend.model.Appointment;

import java.util.List;

public interface AppointmentService {

    Appointment book(Appointment appointment);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    Appointment updateStatus(Long id, String status);

    Appointment saveCallSummary(Long id, String summary);

    /** Mock payment: marks payment as PAID. */
    Appointment markPaymentPaid(Long appointmentId);
}
