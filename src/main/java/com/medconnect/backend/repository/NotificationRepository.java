package com.medconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medconnect.backend.model.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Fetch all notifications for a user, newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Fetch only unread notifications (useful for the red counter badge)
    List<Notification> findByUserIdAndIsReadFalse(Long userId); 

    void deleteByUserId(Long userId);
}