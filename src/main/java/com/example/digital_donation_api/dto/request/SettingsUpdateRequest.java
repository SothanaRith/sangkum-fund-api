package com.example.digital_donation_api.dto.request;

import com.example.digital_donation_api.dto.response.SettingsResponse.NotificationSettings;
import com.example.digital_donation_api.dto.response.SettingsResponse.PrivacySettings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsUpdateRequest {
    private PrivacySettings privacySettings;
    private NotificationSettings notificationSettings;
}
