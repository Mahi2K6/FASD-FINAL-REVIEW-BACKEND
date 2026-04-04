package com.medconnect.backend.service;

import com.medconnect.backend.model.Prescription;

import java.util.List;

public interface PrescriptionService {

    Prescription add(Prescription prescription);

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findPending();

    Prescription dispense(Long id);
}
