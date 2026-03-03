package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.repository.AppointmentRepository;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // 1. Book Appointment (Now includes startTime and endTime)
    @PostMapping("/book")
    public Appointment bookAppointment(@RequestBody Appointment appointment) {
        appointment.setStatus("PENDING");
        return appointmentRepository.save(appointment);
    }

    // 2. Get Doctor's Appointments (Frontend uses this to disable booked slots on the calendar)
    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    // 3. Get Patient's Appointments
    @GetMapping("/patient/{patientId}")
    public List<Appointment> getPatientAppointments(@PathVariable Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    // 4. Update Status (Accept/Reject)
    @PutMapping("/status/{id}")
    public Appointment updateStatus(@PathVariable Long id, @RequestBody String status) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    // 5. Save Video Call Summary (Post-consultation notes)
    @PutMapping("/{id}/summary")
    public Appointment saveCallSummary(@PathVariable Long id, @RequestBody String summary) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setCallSummary(summary);
        appointment.setStatus("COMPLETED"); // Auto-complete when notes are saved
        return appointmentRepository.save(appointment);
    }
}