package com.example.digital_donation_api.dto.request;

import com.example.digital_donation_api.dto.response.SettingsResponse.NotificationSettings;
import com.example.digital_donation_api.dto.response.SettingsResponse.PrivacySettings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsUpdateRequest {
    @jakarta.validation.constraints.NotNull
    private PrivacySettings privacySettings;
    private NotificationSettings notificationSettings;
    private com.example.digital_donation_api.dto.response.SettingsResponse.SecuritySettings securitySettings;
}
