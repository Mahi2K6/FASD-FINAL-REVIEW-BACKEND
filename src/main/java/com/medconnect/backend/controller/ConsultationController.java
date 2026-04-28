package com.medconnect.backend.controller;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.repository.AppointmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/consultation")
@CrossOrigin(origins = "*")
public class ConsultationController {

    private final AppointmentRepository appointmentRepository;

    public ConsultationController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> getConsultationStatus(@PathVariable Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appointment = appointmentOpt.get();
        boolean isActive = !"COMPLETED".equals(appointment.getStatus());

        System.out.println("consultation status check for appointment: " + appointmentId);

        return ResponseEntity.ok(Map.of(
            "doctorJoined", true, // Based on real room presence in a production app
            "patientJoined", true,
            "active", isActive,
            "status", appointment.getStatus()
        ));
    }

    @PostMapping("/end")
    public ResponseEntity<?> endConsultation(@RequestBody Map<String, Long> payload) {
        Long appointmentId = payload.get("appointmentId");
        if (appointmentId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "appointmentId is required"));
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appointment = appointmentOpt.get();
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);

        System.out.println("consultation ended for appointment: " + appointmentId);

        return ResponseEntity.ok(Map.of("message", "Consultation ended and marked as COMPLETED"));
    }
}
