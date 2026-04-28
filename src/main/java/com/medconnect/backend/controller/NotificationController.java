package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Notification;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.NotificationRepository;
import com.medconnect.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> list = notificationRepository.findAll();
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        List<Notification> list = notificationRepository.findByUserIdAndIsReadFalse(userId);
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setRead(true);
                    Notification saved = notificationRepository.save(notification);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> myNotifications(Principal principal) {
        User user = resolveUser(principal);
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(list == null ? List.of() : list);
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        User user = resolveUser(principal);
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(user.getId());
        if (unread != null) {
            for (Notification n : unread) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> clearNotifications(Principal principal) {
        User user = resolveUser(principal);
        notificationRepository.deleteByUserId(user.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications cleared"));
    }

    private User resolveUser(Principal principal) {
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));
    }
}