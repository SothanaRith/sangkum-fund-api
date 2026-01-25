package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.response.TokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Service {
    
    TokenResponse processOAuth2User(OAuth2User oAuth2User, String registrationId);
}
