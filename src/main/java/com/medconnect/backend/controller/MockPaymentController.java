package com.medconnect.backend.controller;

import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.dto.MockPaymentRequest;
import com.medconnect.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class MockPaymentController {

    private final AppointmentService appointmentService;

    public MockPaymentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Marks an appointment as paid (placeholder for future payment gateway integration).
     */
    @PostMapping("/mock")
    public Appointment mockPayment(@Valid @RequestBody MockPaymentRequest request) {
        return appointmentService.markPaymentPaid(request.getAppointmentId());
    }
}
