package com.example.digital_donation_api.service;

public interface NotificationService {
    void notifyUser(Long userId, String type, String data);
}
