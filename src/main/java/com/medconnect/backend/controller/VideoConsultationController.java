package com.medconnect.backend.controller;

import com.medconnect.backend.model.dto.VideoConsultationResponse;
import com.medconnect.backend.service.VideoConsultationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoConsultationController {

    private final VideoConsultationService videoConsultationService;

    public VideoConsultationController(VideoConsultationService videoConsultationService) {
        this.videoConsultationService = videoConsultationService;
    }

    @PostMapping("/start/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> startSession(@PathVariable Long appointmentId) {
        try {
            VideoConsultationResponse response = videoConsultationService.startSession(appointmentId);
            System.out.println("consultation started for appointment: " + appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/session/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSession(@PathVariable Long appointmentId) {
        try {
            VideoConsultationResponse response = videoConsultationService.getSession(appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/end/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> endSession(@PathVariable Long appointmentId) {
        try {
            VideoConsultationResponse response = videoConsultationService.endSession(appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
