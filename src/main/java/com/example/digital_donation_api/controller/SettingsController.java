package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.request.SettingsUpdateRequest;
import com.example.digital_donation_api.dto.response.SettingsResponse;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        SettingsResponse settings = settingsService.getSettings(user.getId());
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<SettingsResponse> updateSettings(
            @jakarta.validation.Valid @RequestBody SettingsUpdateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        SettingsResponse settings = settingsService.updateSettings(
                user.getId(),
                request.getPrivacySettings(),
                request.getNotificationSettings(),
                request.getSecuritySettings()
        );
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/telegram")
    public ResponseEntity<SettingsResponse> connectTelegram(
            @RequestBody Map<String, String> telegramData,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        String chatId = telegramData.get("chatId");
        String username = telegramData.get("username");
        
        SettingsResponse settings = settingsService.connectTelegram(user.getId(), chatId, username);
        return ResponseEntity.ok(settings);
    }

    @DeleteMapping("/telegram")
    public ResponseEntity<SettingsResponse> disconnectTelegram(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        SettingsResponse settings = settingsService.disconnectTelegram(user.getId());
        return ResponseEntity.ok(settings);
    }
}
