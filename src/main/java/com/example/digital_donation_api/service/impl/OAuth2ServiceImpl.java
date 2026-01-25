package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.config.JwtConfig;
import com.example.digital_donation_api.dto.response.TokenResponse;
import com.example.digital_donation_api.entity.RefreshToken;
import com.example.digital_donation_api.entity.Role;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.entity.UserRole;
import com.example.digital_donation_api.repository.RoleRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.repository.UserRoleRepository;
import com.example.digital_donation_api.service.JwtService;
import com.example.digital_donation_api.service.OAuth2Service;
import com.example.digital_donation_api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtConfig jwtConfig;

    @Override
    public TokenResponse processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        
        log.debug("Processing OAuth2 user - Provider: {}, Email: {}", registrationId, email);
        
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuth2User(email, name, picture, registrationId));
        
        // Update avatar if available and not set
        if (picture != null && (user.getAvatar() == null || user.getAvatar().isEmpty())) {
            user.setAvatar(picture);
            userRepository.save(user);
        }
        
        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        log.debug("OAuth2 tokens generated successfully for user: {}", user.getEmail());
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .build();
    }
    
    private User createOAuth2User(String email, String name, String picture, String provider) {
        log.debug("Creating new OAuth2 user: {}", email);
        
        User user = new User();
        user.setName(name != null ? name : email.split("@")[0]);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
        user.setAvatar(picture);
        user.setIsActive(true);
        
        userRepository.save(user);
        
        // Assign default DONOR role
        Role donorRole = roleRepository.findByName("DONOR")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("DONOR");
                    return roleRepository.save(role);
                });
        
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(donorRole);
        userRoleRepository.save(userRole);
        
        log.debug("OAuth2 user created successfully: {}", email);
        
        return user;
    }
}
