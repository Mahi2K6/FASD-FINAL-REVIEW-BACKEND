package com.medconnect.backend.controller;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.dto.AppointmentResponseDTO;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.AppointmentService;
import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.exception.SlotAlreadyBookedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(appointment));
        } catch (SlotAlreadyBookedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Slot already booked"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(appointment));
        } catch (SlotAlreadyBookedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Slot already booked"));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        System.out.println("Fetching appointments...");
        List<AppointmentResponseDTO> result = appointmentService.findByDoctorId(doctorId);
        return ResponseEntity.ok(result == null ? List.of() : result);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getPatientAppointments(@PathVariable Long patientId) {
        System.out.println("Fetching appointments...");
        List<AppointmentResponseDTO> result = appointmentService.findByPatientId(patientId);
        return ResponseEntity.ok(result == null ? List.of() : result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        Appointment updated = appointmentService.updateAppointment(id, appointment);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        if (appointmentService.existsById(id)) {
            appointmentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Appointment> updateStatus(@PathVariable Long id, @RequestBody String status) {
        Appointment updated = appointmentService.updateStatus(id, status);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/summary")
    public ResponseEntity<Appointment> saveCallSummary(@PathVariable Long id, @RequestBody String summary) {
        Appointment updated = appointmentService.saveCallSummary(id, summary);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponseDTO>> myAppointments(Principal principal) {
        System.out.println("Fetching appointments...");
        User user = userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));
        if (user.getRole() == Role.DOCTOR) {
            List<AppointmentResponseDTO> result = appointmentService.findByDoctorId(user.getId());
            return ResponseEntity.ok(result == null ? List.of() : result);
        }
        List<AppointmentResponseDTO> result = appointmentService.findByPatientId(user.getId());
        return ResponseEntity.ok(result == null ? List.of() : result);
    }
}