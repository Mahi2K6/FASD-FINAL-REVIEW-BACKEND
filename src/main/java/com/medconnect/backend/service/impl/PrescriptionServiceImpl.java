package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.repository.PrescriptionRepository;
import com.medconnect.backend.service.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    @Transactional
    public Prescription add(Prescription prescription) {
        prescription.setStatus("PENDING");
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
        return prescriptionRepository.findByStatus("PENDING");
    }

    @Override
    @Transactional
    public Prescription dispense(Long id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
        p.setStatus("DISPENSED");
        return prescriptionRepository.save(p);
    }
}
