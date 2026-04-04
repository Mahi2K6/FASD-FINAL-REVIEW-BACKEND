package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional
    public Appointment book(Appointment appointment) {
        appointment.setStatus("PENDING");
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional
    public Appointment updateStatus(Long id, String status) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        a.setStatus(status);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment saveCallSummary(Long id, String summary) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        a.setCallSummary(summary);
        a.setStatus("COMPLETED");
        return appointmentRepository.save(a);
    }
}
