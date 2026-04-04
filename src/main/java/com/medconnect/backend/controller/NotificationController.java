package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medconnect.backend.model.Notification;
import com.medconnect.backend.repository.NotificationRepository;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // Fetch all notifications for the dropdown
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Fetch ONLY unread notifications to show the red badge number
    @GetMapping("/user/{userId}/unread")
    public List<Notification> getUnreadNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    // Mark a single notification as read when clicked
    @PutMapping("/read/{id}")
    public Notification markAsRead(@PathVariable Long id) {
        Notification notif = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notif.setRead(true);
        return notificationRepository.save(notif);
    }
}