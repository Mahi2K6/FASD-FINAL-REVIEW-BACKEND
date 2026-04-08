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

    @GetMapping
    public List<Prescription> getPrescriptions() {
        System.out.println("Fetching prescriptions...");
        List<Prescription> list = prescriptionService.findPending();
        return list == null ? List.of() : list;
    }

    @PostMapping("/add")
    public Prescription addPrescription(@RequestBody Prescription prescription) {
        return prescriptionService.add(prescription);
    }

    @GetMapping("/patient/{patientId}")
    public List<Prescription> getMyPrescriptions(@PathVariable Long patientId) {
        System.out.println("Fetching prescriptions...");
        List<Prescription> list = prescriptionService.findByPatientId(patientId);
        return list == null ? List.of() : list;
    }

    @GetMapping("/pharmacist/pending")
    public List<Prescription> getAllPending() {
        System.out.println("Fetching prescriptions...");
        List<Prescription> list = prescriptionService.findPending();
        return list == null ? List.of() : list;
    }

    @PutMapping("/dispense/{id}")
    public Prescription dispenseMedicine(@PathVariable Long id) {
        return prescriptionService.dispense(id);
    }
}
