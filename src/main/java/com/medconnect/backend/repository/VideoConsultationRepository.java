package com.medconnect.backend.repository;

import com.medconnect.backend.model.VideoConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoConsultationRepository extends JpaRepository<VideoConsultation, Long> {

    Optional<VideoConsultation> findByAppointmentId(Long appointmentId);

    Optional<VideoConsultation> findByRoomId(String roomId);

    boolean existsByAppointmentId(Long appointmentId);
}
