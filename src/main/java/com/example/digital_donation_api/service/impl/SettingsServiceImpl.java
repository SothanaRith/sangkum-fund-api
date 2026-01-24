package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.dto.response.SettingsResponse;
import com.example.digital_donation_api.dto.response.SettingsResponse.NotificationSettings;
import com.example.digital_donation_api.dto.response.SettingsResponse.PrivacySettings;
import com.example.digital_donation_api.dto.response.SettingsResponse.TelegramSettings;
import com.example.digital_donation_api.entity.Settings;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.SettingsRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.SettingsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingsServiceImpl implements SettingsService {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public SettingsResponse getSettings(Long userId) {
        Settings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        return mapToResponse(settings);
    }

    @Override
    public SettingsResponse updateSettings(Long userId, PrivacySettings privacySettings, NotificationSettings notificationSettings) {
        Settings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        try {
            if (privacySettings != null) {
                settings.setPrivacySettings(objectMapper.writeValueAsString(privacySettings));
            }
            if (notificationSettings != null) {
                settings.setNotificationSettings(objectMapper.writeValueAsString(notificationSettings));
            }
            
            settings = settingsRepository.save(settings);
            return mapToResponse(settings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to update settings", e);
        }
    }

    @Override
    public SettingsResponse connectTelegram(Long userId, String chatId, String username) {
        Settings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        try {
            TelegramSettings telegramSettings = new TelegramSettings(true, chatId, username);
            
            // Update notification settings to enable telegram
            NotificationSettings notifSettings = parseNotificationSettings(settings.getNotificationSettings());
            notifSettings.setTelegramNotifications(true);
            settings.setNotificationSettings(objectMapper.writeValueAsString(notifSettings));
            
            settings = settingsRepository.save(settings);
            
            SettingsResponse response = mapToResponse(settings);
            response.setTelegramSettings(telegramSettings);
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to connect Telegram", e);
        }
    }

    @Override
    public SettingsResponse disconnectTelegram(Long userId) {
        Settings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        try {
            // Update notification settings to disable telegram
            NotificationSettings notifSettings = parseNotificationSettings(settings.getNotificationSettings());
            notifSettings.setTelegramNotifications(false);
            settings.setNotificationSettings(objectMapper.writeValueAsString(notifSettings));
            
            settings = settingsRepository.save(settings);
            
            SettingsResponse response = mapToResponse(settings);
            response.setTelegramSettings(new TelegramSettings(false, null, null));
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to disconnect Telegram", e);
        }
    }

    private Settings createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Settings settings = new Settings();
        settings.setUser(user);
        
        try {
            PrivacySettings defaultPrivacy = new PrivacySettings(true, true, false);
            NotificationSettings defaultNotifications = new NotificationSettings(true, true, true, true, false);
            
            settings.setPrivacySettings(objectMapper.writeValueAsString(defaultPrivacy));
            settings.setNotificationSettings(objectMapper.writeValueAsString(defaultNotifications));
            
            return settingsRepository.save(settings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create default settings", e);
        }
    }

    private SettingsResponse mapToResponse(Settings settings) {
        PrivacySettings privacySettings = parsePrivacySettings(settings.getPrivacySettings());
        NotificationSettings notificationSettings = parseNotificationSettings(settings.getNotificationSettings());
        TelegramSettings telegramSettings = new TelegramSettings(
            notificationSettings.getTelegramNotifications() != null && notificationSettings.getTelegramNotifications(),
            null,
            null
        );
        
        return new SettingsResponse(
            settings.getId(),
            settings.getUser().getId(),
            privacySettings,
            notificationSettings,
            telegramSettings
        );
    }

    private PrivacySettings parsePrivacySettings(String json) {
        if (json == null || json.isEmpty()) {
            return new PrivacySettings(true, true, false);
        }
        try {
            return objectMapper.readValue(json, PrivacySettings.class);
        } catch (JsonProcessingException e) {
            return new PrivacySettings(true, true, false);
        }
    }

    private NotificationSettings parseNotificationSettings(String json) {
        if (json == null || json.isEmpty()) {
            return new NotificationSettings(true, true, true, true, false);
        }
        try {
            return objectMapper.readValue(json, NotificationSettings.class);
        } catch (JsonProcessingException e) {
            return new NotificationSettings(true, true, true, true, false);
        }
    }
}
