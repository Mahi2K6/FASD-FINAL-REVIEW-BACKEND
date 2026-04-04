package com.medconnect.backend.controller;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public Appointment bookAppointment(@RequestBody Appointment appointment) {
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
}
