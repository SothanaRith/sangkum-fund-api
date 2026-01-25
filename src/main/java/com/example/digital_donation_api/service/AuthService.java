package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.response.TokenResponse;
import com.example.digital_donation_api.entity.User;

public interface AuthService {

    TokenResponse login(String email, String password);
    
    TokenResponse loginWithOtp(String email, String password, String otpCode);

    User register(String name, String email, String password);
    
    User registerWithOtp(String name, String email, String password, String otpCode);
    
    String sendLoginOtp(String email);
    
    String sendRegistrationOtp(String email);
    
    TokenResponse refreshToken(String refreshToken);
    
    void logout(String refreshToken);
}
