package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.OtpCode;
import com.example.digital_donation_api.entity.OtpType;
import com.example.digital_donation_api.exception.BadRequestException;
import com.example.digital_donation_api.repository.OtpCodeRepository;
import com.example.digital_donation_api.service.EmailService;
import com.example.digital_donation_api.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    
    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;
    
    @Value("${otp.max-attempts:3}")
    private int maxAttempts;
    
    private static final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public String generateAndSendOtp(String email, OtpType type) {
        // Delete any existing unverified OTPs for this email and type
        otpCodeRepository.deleteUnverifiedOtpsByEmailAndType(email, type);
        
        // Generate 6-digit OTP
        String code = String.format("%06d", random.nextInt(1000000));
        
        // Create and save OTP
        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .verified(false)
                .attempts(0)
                .build();
        
        otpCodeRepository.save(otpCode);
        
        // Send email
        String purpose = switch (type) {
            case REGISTRATION -> "Registration";
            case LOGIN -> "Login";
            case PASSWORD_RESET -> "Password Reset";
        };
        
        emailService.sendOtpEmail(email, code, purpose);
        
        log.info("OTP generated and sent for email: {} and type: {}", email, type);
        return code; // In production, don't return the code
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String code, OtpType type) {
        Optional<OtpCode> otpCodeOpt = otpCodeRepository
                .findByEmailAndCodeAndTypeAndVerifiedFalse(email, code, type);
        
        if (otpCodeOpt.isEmpty()) {
            log.warn("OTP verification failed - OTP not found for email: {}", email);
            return false;
        }
        
        OtpCode otpCode = otpCodeOpt.get();
        
        // Check if expired
        if (otpCode.isExpired()) {
            log.warn("OTP verification failed - OTP expired for email: {}", email);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }
        
        // Check attempts
        if (otpCode.getAttempts() >= maxAttempts) {
            log.warn("OTP verification failed - Max attempts reached for email: {}", email);
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP.");
        }
        
        // Increment attempts
        otpCode.setAttempts(otpCode.getAttempts() + 1);
        
        // Verify code
        if (!otpCode.getCode().equals(code)) {
            otpCodeRepository.save(otpCode);
            log.warn("OTP verification failed - Invalid code for email: {}", email);
            return false;
        }
        
        // Mark as verified
        otpCode.setVerified(true);
        otpCode.setVerifiedAt(LocalDateTime.now());
        otpCodeRepository.save(otpCode);
        
        log.info("OTP verified successfully for email: {} and type: {}", email, type);
        return true;
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Expired OTPs cleaned up");
    }
}
