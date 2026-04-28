package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.VideoConsultation;
import com.medconnect.backend.model.dto.VideoConsultationResponse;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.repository.VideoConsultationRepository;
import com.medconnect.backend.service.VideoConsultationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class VideoConsultationServiceImpl implements VideoConsultationService {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final VideoConsultationRepository videoConsultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public VideoConsultationServiceImpl(
            VideoConsultationRepository videoConsultationRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository
    ) {
        this.videoConsultationRepository = videoConsultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public VideoConsultationResponse startSession(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        // If session already exists, return it
        VideoConsultation existing = videoConsultationRepository.findByAppointmentId(appointmentId).orElse(null);
        if (existing != null && !"COMPLETED".equals(existing.getStatus()) && !"CANCELLED".equals(existing.getStatus())) {
            System.out.println("Returning existing video session for appointmentId=" + appointmentId);
            return toResponse(existing);
        }

        // Generate unique room ID
        String roomId = "medconnect-" + appointmentId + "-" + UUID.randomUUID().toString().substring(0, 8);

        VideoConsultation session = new VideoConsultation();
        session.setAppointmentId(appointmentId);
        session.setDoctorId(appointment.getDoctorId());
        session.setPatientId(appointment.getPatientId());
        session.setRoomId(roomId);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus("IN_PROGRESS");

        VideoConsultation saved = videoConsultationRepository.save(session);

        // Mark appointment as IN_PROGRESS
        appointment.setStatus("IN_PROGRESS");
        appointmentRepository.save(appointment);

        System.out.println("Video session started: " + saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoConsultationResponse getSession(Long appointmentId) {
        VideoConsultation session = videoConsultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No video session found for appointment: " + appointmentId));
        return toResponse(session);
    }

    @Override
    @Transactional
    public VideoConsultationResponse endSession(Long appointmentId) {
        VideoConsultation session = videoConsultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No video session found for appointment: " + appointmentId));

        session.setEndedAt(LocalDateTime.now());
        session.setStatus("COMPLETED");
        VideoConsultation saved = videoConsultationRepository.save(session);

        // Mark appointment as COMPLETED
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment != null) {
            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);
        }

        System.out.println("Video session ended: " + saved);
        return toResponse(saved);
    }

    private VideoConsultationResponse toResponse(VideoConsultation session) {
        VideoConsultationResponse response = new VideoConsultationResponse();
        response.setId(session.getId());
        response.setAppointmentId(session.getAppointmentId());
        response.setDoctorId(session.getDoctorId());
        response.setPatientId(session.getPatientId());
        response.setRoomId(session.getRoomId());
        response.setStartedAt(session.getStartedAt() != null ? session.getStartedAt().format(DT_FORMAT) : null);
        response.setEndedAt(session.getEndedAt() != null ? session.getEndedAt().format(DT_FORMAT) : null);
        response.setStatus(session.getStatus());
        response.setNotes(session.getNotes());
        response.setCreatedAt(session.getCreatedAt() != null ? session.getCreatedAt().format(DT_FORMAT) : null);

        // Enrich with names
        if (session.getDoctorId() != null) {
            userRepository.findById(session.getDoctorId())
                    .ifPresent(doctor -> response.setDoctorName(doctor.getName()));
        }
        if (session.getPatientId() != null) {
            userRepository.findById(session.getPatientId())
                    .ifPresent(patient -> response.setPatientName(patient.getName()));
        }

        return response;
    }
}
