package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.RefreshToken;
import com.example.digital_donation_api.entity.User;

public interface RefreshTokenService {
    
    RefreshToken createRefreshToken(User user);
    
    RefreshToken verifyRefreshToken(String token);
    
    void revokeRefreshToken(String token);
    
    void revokeAllUserTokens(User user);
}
