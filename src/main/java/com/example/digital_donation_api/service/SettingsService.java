package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.response.SettingsResponse;
import com.example.digital_donation_api.dto.response.SettingsResponse.NotificationSettings;
import com.example.digital_donation_api.dto.response.SettingsResponse.PrivacySettings;

public interface SettingsService {
    SettingsResponse getSettings(Long userId);
    
    SettingsResponse updateSettings(Long userId, PrivacySettings privacySettings, NotificationSettings notificationSettings, com.example.digital_donation_api.dto.response.SettingsResponse.SecuritySettings securitySettings);
    
    SettingsResponse connectTelegram(Long userId, String chatId, String username);
    
    SettingsResponse disconnectTelegram(Long userId);
}
