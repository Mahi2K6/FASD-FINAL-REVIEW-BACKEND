package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.repository.PrescriptionRepository;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    // 1. Doctor creates a prescription
    @PostMapping("/add")
    public Prescription addPrescription(@RequestBody Prescription prescription) {
        prescription.setStatus("PENDING");
        return prescriptionRepository.save(prescription);
    }

    // 2. Patient sees their prescriptions
    @GetMapping("/patient/{patientId}")
    public List<Prescription> getMyPrescriptions(@PathVariable Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    // 3. Pharmacist sees ALL pending prescriptions
    @GetMapping("/pharmacist/pending")
    public List<Prescription> getAllPending() {
        return prescriptionRepository.findByStatus("PENDING");
    }

    // 4. Pharmacist marks as Dispensed
    @PutMapping("/dispense/{id}")
    public Prescription dispenseMedicine(@PathVariable Long id) {
        Prescription p = prescriptionRepository.findById(id).orElseThrow();
        p.setStatus("DISPENSED");
        return prescriptionRepository.save(p);
    }
}