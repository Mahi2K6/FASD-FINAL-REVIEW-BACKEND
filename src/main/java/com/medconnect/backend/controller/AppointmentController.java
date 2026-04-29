package com.medconnect.backend.controller;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.dto.AppointmentResponseDTO;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.AppointmentService;
import com.medconnect.backend.repository.AppointmentRepository;
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
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository, AppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment) {
        System.out.println("Appointment payload: " + appointment);
        
        if (appointment.getDoctorId() == null) {
            return ResponseEntity.badRequest().body("Doctor ID missing");
        }
        if (appointment.getPatientId() == null) {
            return ResponseEntity.badRequest().body("Patient ID missing");
        }
        if (appointment.getSlotId() == null) {
            return ResponseEntity.badRequest().body("Slot ID missing");
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(appointment));
        } catch (SlotAlreadyBookedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Slot already booked"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage() != null ? ex.getMessage() : "Unknown error");
        }
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment request) {
        System.out.println("Parsed Appointment:");
        System.out.println(request);
        try {
            // Validate required fields
            if (request.getDoctorId() == null) {
                return ResponseEntity.badRequest().body("Doctor ID missing");
            }
            if (request.getPatientId() == null) {
                return ResponseEntity.badRequest().body("Patient ID missing");
            }
            if (request.getSlotId() == null) {
                return ResponseEntity.badRequest().body("Slot ID missing");
            }

            // Verify entities exist
            if (!userRepository.existsById(request.getDoctorId())) {
                return ResponseEntity.badRequest().body("Doctor not found");
            }
            if (!userRepository.existsById(request.getPatientId())) {
                return ResponseEntity.badRequest().body("Patient not found");
            }

            if (request.getStatus() == null) {
                request.setStatus("PENDING");
            }
            if (request.getPaymentStatus() == null) {
                request.setPaymentStatus(com.medconnect.backend.model.PaymentStatus.SUCCESS);
            }
            if (request.getAppointmentDate() == null && request.getDate() != null) {
                try {
                    java.time.LocalDate ld = java.time.LocalDate.parse(request.getDate());
                    request.setAppointmentDate(ld);
                } catch (Exception e) {
                    System.out.println("Could not parse date: " + request.getDate());
                }
            }
            if (request.getStartTime() == null && request.getTime() != null) {
                try {
                    request.setStartTime(java.time.LocalTime.parse(request.getTime()));
                } catch (Exception e) {
                    System.out.println("Could not parse time: " + request.getTime());
                }
            }

            Appointment savedAppointment = appointmentRepository.save(request);
            System.out.println("Saved appointment: " + savedAppointment);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "appointmentId", savedAppointment.getId()
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Unknown error occurred";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "reason", errorMsg
            ));
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

    @GetMapping("/history")
    public ResponseEntity<List<AppointmentResponseDTO>> appointmentHistory(Principal principal) {
        User user = userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        List<AppointmentResponseDTO> result;
        if (user.getRole() == Role.DOCTOR) {
            result = appointmentService.findByDoctorId(user.getId());
        } else if (user.getRole() == Role.ADMIN) {
            // Admin gets all appointments
            result = appointmentService.findAll();
        } else {
            result = appointmentService.findByPatientId(user.getId());
        }
        return ResponseEntity.ok(result != null ? result : List.of());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonErrors(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        System.out.println("JSON Parsing Error: " + ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid JSON body: " + ex.getMostSpecificCause().getMessage());
    }
}