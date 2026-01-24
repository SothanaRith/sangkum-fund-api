package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SettingsResponse {
    private Long id;
    private Long userId;
    private PrivacySettings privacySettings;
    private NotificationSettings notificationSettings;
    private TelegramSettings telegramSettings;
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class PrivacySettings {
        private Boolean profileVisible;
        private Boolean showDonations;
        private Boolean showEmail;
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class NotificationSettings {
        private Boolean emailNotifications;
        private Boolean donationAlerts;
        private Boolean eventUpdates;
        private Boolean milestoneAlerts;
        private Boolean telegramNotifications;
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class TelegramSettings {
        private Boolean connected;
        private String chatId;
        private String username;
    }
}
