package com.medconnect.backend.service;

import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.model.dto.PrescriptionCreateRequest;
import com.medconnect.backend.model.dto.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {

    Prescription add(Prescription prescription, String email);

    List<Prescription> findByPatientId(Long patientId, String email);

    List<Prescription> findByDoctorId(Long doctorId, String email);

    Prescription findByAppointmentId(Long appointmentId, String email);

    List<Prescription> findPending();

    Prescription dispense(Long id);

    Prescription update(Long id, Prescription prescription);

    void delete(Long id);

    // --- Marketplace extensions ---

    PrescriptionResponse createWithMedicines(PrescriptionCreateRequest request);

    PrescriptionResponse getLatestByPatientId(Long patientId);

    List<PrescriptionResponse> getHistoryByPatientId(Long patientId);
}
