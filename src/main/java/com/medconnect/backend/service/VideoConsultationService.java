package com.medconnect.backend.service;

import com.medconnect.backend.model.dto.VideoConsultationResponse;

public interface VideoConsultationService {

    VideoConsultationResponse startSession(Long appointmentId);

    VideoConsultationResponse getSession(Long appointmentId);

    VideoConsultationResponse endSession(Long appointmentId);
}
