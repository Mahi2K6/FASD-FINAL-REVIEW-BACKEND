package com.medconnect.backend.controller;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.AppointmentService;
import com.medconnect.backend.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
    public Appointment bookAppointment(@RequestBody Appointment appointment) {
        return appointmentService.book(appointment);
    }

    @PostMapping
    public Appointment createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.book(appointment);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentService.findByDoctorId(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> getPatientAppointments(@PathVariable Long patientId) {
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
    public List<Appointment> myAppointments(Principal principal) {
        User user = userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));
        if (user.getRole() == Role.DOCTOR) {
            return appointmentService.findByDoctorId(user.getId());
        }
        return appointmentService.findByPatientId(user.getId());
    }
}
