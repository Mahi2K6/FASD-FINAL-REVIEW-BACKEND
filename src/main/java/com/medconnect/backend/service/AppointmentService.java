package com.medconnect.backend.service;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.dto.AppointmentResponseDTO;

import java.util.List;

public interface AppointmentService {

    Appointment book(Appointment appointment);

    List<AppointmentResponseDTO> findByDoctorId(Long doctorId);

    List<AppointmentResponseDTO> findByPatientId(Long patientId);

    /** Same as {@link #findByDoctorId(Long)}; kept for existing call sites. */
    List<AppointmentResponseDTO> findByDoctorIdEnriched(Long doctorId);

    Appointment updateStatus(Long id, String status);

    Appointment saveCallSummary(Long id, String summary);

    /** Mock payment: marks payment as PAID. */
    Appointment markPaymentPaid(Long appointmentId);

    Appointment updateAppointment(Long id, Appointment appointment);

    void deleteById(Long id);

    boolean existsById(Long id);

    List<AppointmentResponseDTO> findAll();
}
