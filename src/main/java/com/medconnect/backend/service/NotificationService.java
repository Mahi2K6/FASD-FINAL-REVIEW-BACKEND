package com.medconnect.backend.service;

public interface NotificationService {

    void createNotification(Long userId, String title, String message, String type);
}
