package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.config.JwtConfig;
import com.example.digital_donation_api.entity.RefreshToken;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.TokenException;
import com.example.digital_donation_api.repository.RefreshTokenRepository;
import com.example.digital_donation_api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke existing tokens for this user
        revokeAllUserTokens(user);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpiration()))
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));
        
        if (refreshToken.getRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }
        
        if (refreshToken.isExpired()) {
            throw new TokenException("Refresh token has expired");
        }
        
        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));
        
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }
}
