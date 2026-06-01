package com.example.digital_donation_api.dto.request;

import com.example.digital_donation_api.dto.response.SettingsResponse.NotificationSettings;
import com.example.digital_donation_api.dto.response.SettingsResponse.PrivacySettings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingsUpdateRequest {
    private PrivacySettings privacySettings;
    private NotificationSettings notificationSettings;
    private com.example.digital_donation_api.dto.response.SettingsResponse.SecuritySettings securitySettings;
}
