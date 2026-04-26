package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.repository.PrescriptionRepository;
import com.medconnect.backend.service.NotificationService;
import com.medconnect.backend.service.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final NotificationService notificationService;

    public PrescriptionServiceImpl(
            PrescriptionRepository prescriptionRepository,
            NotificationService notificationService
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Prescription add(Prescription prescription) {
        prescription.setStatus("ACTIVE");
        return prescriptionRepository.save(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prescription> findByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prescription> findPending() {
        return prescriptionRepository.findByStatus("ACTIVE");
    }

    @Override
    @Transactional
    public Prescription dispense(Long id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
        p.setStatus("COMPLETED");
        Prescription saved = prescriptionRepository.save(p);
        notificationService.createNotification(
                saved.getPatientId(),
                "Prescription Ready",
                "Your prescription has been dispensed.",
                "PRESCRIPTION"
        );
        return saved;
    }

    @Override
    @Transactional
    public Prescription update(Long id, Prescription prescription) {
        Prescription existing = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
        existing.setPatientId(prescription.getPatientId());
        existing.setDoctorId(prescription.getDoctorId());
        existing.setAppointmentId(prescription.getAppointmentId());
        existing.setMedicines(prescription.getMedicines());
        existing.setStatus(prescription.getStatus());
        return prescriptionRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        prescriptionRepository.deleteById(id);
    }
}