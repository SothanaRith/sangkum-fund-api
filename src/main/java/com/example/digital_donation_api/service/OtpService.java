package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.OtpType;

public interface OtpService {
    
    String generateAndSendOtp(String email, OtpType type);
    
    boolean verifyOtp(String email, String code, OtpType type);
    
    void cleanupExpiredOtps();
}
