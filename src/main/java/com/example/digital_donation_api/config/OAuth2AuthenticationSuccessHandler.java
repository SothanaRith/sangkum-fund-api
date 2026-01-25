package com.example.digital_donation_api.config;

import com.example.digital_donation_api.dto.response.TokenResponse;
import com.example.digital_donation_api.service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oAuth2Service;
    
    @Value("${oauth2.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        try {
            // Process OAuth2 user and generate tokens
            String registrationId = extractRegistrationId(request);
            TokenResponse tokenResponse = oAuth2Service.processOAuth2User(oAuth2User, registrationId);
            
            // Redirect to frontend with tokens as URL parameters
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/oauth2/redirect")
                    .queryParam("accessToken", tokenResponse.getAccessToken())
                    .queryParam("refreshToken", tokenResponse.getRefreshToken())
                    .queryParam("tokenType", tokenResponse.getTokenType())
                    .queryParam("expiresIn", tokenResponse.getExpiresIn())
                    .build()
                    .toUriString();
            
            log.debug("Redirecting to: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 authentication error", e);
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/login")
                    .queryParam("error", "oauth2_failed")
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
    
    private String extractRegistrationId(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // Extract from URI like: /api/auth/oauth2/callback/google
        String[] parts = requestUri.split("/");
        return parts[parts.length - 1];
    }
}
