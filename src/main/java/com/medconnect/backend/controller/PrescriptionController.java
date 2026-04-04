package com.medconnect.backend.controller;

import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/add")
    public Prescription addPrescription(@RequestBody Prescription prescription) {
        return prescriptionService.add(prescription);
    }

    @GetMapping("/patient/{patientId}")
    public List<Prescription> getMyPrescriptions(@PathVariable Long patientId) {
        return prescriptionService.findByPatientId(patientId);
    }

    @GetMapping("/pharmacist/pending")
    public List<Prescription> getAllPending() {
        return prescriptionService.findPending();
    }

    @PutMapping("/dispense/{id}")
    public Prescription dispenseMedicine(@PathVariable Long id) {
        return prescriptionService.dispense(id);
    }
}
