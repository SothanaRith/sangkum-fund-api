package com.example.digital_donation_api.service;

public interface EmailService {
    
    void sendOtpEmail(String to, String otp, String purpose);
    
    void sendWelcomeEmail(String to, String name);
}
