package com.medconnect.backend.controller;

import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Prescription>> getPrescriptions() {
        System.out.println("Fetching prescriptions...");
        List<Prescription> list = prescriptionService.findPending();
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @PostMapping
    public ResponseEntity<Prescription> addPrescription(@RequestBody Prescription prescription) {
        Prescription saved = prescriptionService.add(prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getMyPrescriptions(@PathVariable Long patientId) {
        System.out.println("Fetching prescriptions...");
        List<Prescription> list = prescriptionService.findByPatientId(patientId);
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @GetMapping("/pharmacist/pending")
    public ResponseEntity<List<Prescription>> getAllPending() {
        System.out.println("Fetching prescriptions...");
        List<Prescription> list = prescriptionService.findPending();
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> updatePrescription(@PathVariable Long id, @RequestBody Prescription prescription) {
        Prescription updated = prescriptionService.update(id, prescription);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dispense/{id}")
    public ResponseEntity<Prescription> dispenseMedicine(@PathVariable Long id) {
        Prescription updated = prescriptionService.dispense(id);
        return ResponseEntity.ok(updated);
    }
}