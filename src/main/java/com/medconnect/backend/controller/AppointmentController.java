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
    public List<Appointment> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentService.findByDoctorId(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponseDTO> getPatientAppointments(@PathVariable Long patientId) {
        return appointmentService.findByPatientId(patientId);
    }

    @PutMapping("/status/{id}")
    public Appointment updateStatus(@PathVariable Long id, @RequestBody String status) {
        return appointmentService.updateStatus(id, status);
    }

    @PutMapping("/{id}/summary")
    public Appointment saveCallSummary(@PathVariable Long id, @RequestBody String summary) {
        return appointmentService.saveCallSummary(id, summary);
    }

    @GetMapping("/my")
    public List<AppointmentResponseDTO> myAppointments(Principal principal) {
        User user = userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));
        if (user.getRole() == Role.DOCTOR) {
            return appointmentService.findByDoctorIdEnriched(user.getId());
        }
        return appointmentService.findByPatientId(user.getId());
    }
}
