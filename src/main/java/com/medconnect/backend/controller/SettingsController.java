package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserSettings;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.repository.UserSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public SettingsController(UserSettingsRepository settingsRepository, UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSettings> getMySettings(Principal principal) {
        User user = resolveUser(principal);
        UserSettings settings = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user.getId()));
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSettings> updateSettings(
            Principal principal,
            @RequestBody UserSettings incoming
    ) {
        User user = resolveUser(principal);
        UserSettings settings = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user.getId()));

        // Apply updates
        settings.setDarkMode(incoming.isDarkMode());
        settings.setEmailNotifications(incoming.isEmailNotifications());
        settings.setAppointmentReminders(incoming.isAppointmentReminders());
        settings.setPharmacyUpdates(incoming.isPharmacyUpdates());
        settings.setPrescriptionUpdates(incoming.isPrescriptionUpdates());
        settings.setPrivacyPhoneVisible(incoming.isPrivacyPhoneVisible());
        settings.setShareMedicalRecords(incoming.isShareMedicalRecords());

        UserSettings saved = settingsRepository.save(settings);
        return ResponseEntity.ok(saved);
    }

    private User resolveUser(Principal principal) {
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserSettings createDefaultSettings(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        return settingsRepository.save(settings);
    }
}
