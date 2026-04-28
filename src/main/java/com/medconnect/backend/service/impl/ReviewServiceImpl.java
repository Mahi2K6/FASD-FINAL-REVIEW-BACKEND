package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Review;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.ReviewRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Review addReview(Review review, String patientEmail) {
        System.out.println("Review Save Started");
        System.out.println("Payload: " + review);

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not authenticated"));

        if (review.getAppointmentId() == null || review.getPatientId() == null || review.getDoctorId() == null) {
            throw new IllegalArgumentException("Appointment ID, Patient ID, and Doctor ID are required");
        }

        if (reviewRepository.existsByAppointmentId(review.getAppointmentId())) {
            throw new IllegalArgumentException("Review already submitted for this appointment");
        }

        com.medconnect.backend.model.Appointment appointment = appointmentRepository.findById(review.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatientId().equals(patient.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the assigned patient can review this appointment");
        }

        if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Consultation must be completed before reviewing");
        }

        try {
            return reviewRepository.save(review);
        } catch (Exception e) {
            throw new RuntimeException("Database save failed: " + e.getMessage(), e);
        }
    }
}
