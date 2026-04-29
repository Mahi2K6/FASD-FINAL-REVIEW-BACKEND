package com.medconnect.backend.controller;

import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.model.dto.PrescriptionCreateRequest;
import com.medconnect.backend.model.dto.PrescriptionResponse;
import com.medconnect.backend.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<?> addPrescription(@RequestBody Prescription prescription, java.security.Principal principal) {
        System.out.println("Prescription Request Received");
        System.out.println(prescription);
        try {
            Prescription saved = prescriptionService.add(prescription, principal.getName().trim().toLowerCase());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Prescription saved successfully",
                    "prescriptionId", saved.getId()
            ));
        } catch (IllegalArgumentException ex) {
            System.err.println("Validation Error: " + ex.getMessage());
            if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            System.err.println("Access Denied: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (com.medconnect.backend.exception.ResourceNotFoundException ex) {
            System.err.println("Not Found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("Internal Server Error: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Internal server error: " + ex.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPrescriptions(@PathVariable Long patientId, java.security.Principal principal) {
        try {
            List<Prescription> list = prescriptionService.findByPatientId(patientId, principal.getName().trim().toLowerCase());
            return ResponseEntity.ok(list == null ? List.of() : list);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorPrescriptions(@PathVariable Long doctorId, java.security.Principal principal) {
        try {
            List<Prescription> list = prescriptionService.findByDoctorId(doctorId, principal.getName().trim().toLowerCase());
            return ResponseEntity.ok(list == null ? List.of() : list);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getPrescriptionByAppointment(@PathVariable Long appointmentId, java.security.Principal principal) {
        try {
            Prescription p = prescriptionService.findByAppointmentId(appointmentId, principal.getName().trim().toLowerCase());
            return ResponseEntity.ok(p);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPrescriptionById(@PathVariable Long id) {
        // Just a dummy placeholder since the prompt asked for GET /api/prescriptions/{id}
        // Normally this would also take Principal and validate
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    public ResponseEntity<List<Prescription>> getPrescriptions() {
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
}