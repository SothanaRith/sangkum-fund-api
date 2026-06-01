package com.example.digital_donation_api.service;

public interface NotificationService {
    void notifyUser(Long userId, String type, String data);

    void notify(Long userId, String title, String message, String type, String actionUrl, Long relatedId, String relatedType);
}
