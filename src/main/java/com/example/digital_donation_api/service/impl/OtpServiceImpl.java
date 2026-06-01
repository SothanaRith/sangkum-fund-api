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
    public void generateAndSendOtp(String email, OtpType type) {
        // Invalidate any existing unverified OTP for this email+type
        otpCodeRepository.deleteUnverifiedOtpsByEmailAndType(email, type);

        String code = String.format("%06d", random.nextInt(1000000));

        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .verified(false)
                .attempts(0)
                .build();

        otpCodeRepository.save(otpCode);

        String purpose = switch (type) {
            case REGISTRATION  -> "Registration";
            case LOGIN         -> "Login";
            case PASSWORD_RESET -> "Password Reset";
        };

        emailService.sendOtpEmail(email, code, purpose);
        log.info("OTP sent for email: {} type: {}", email, type);
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String code, OtpType type) {
        // Look up by email+type only — this way wrong-code attempts still count
        Optional<OtpCode> otpCodeOpt = otpCodeRepository
                .findFirstByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(email, type);

        if (otpCodeOpt.isEmpty()) {
            log.warn("OTP not found for email: {} type: {}", email, type);
            return false;
        }

        OtpCode otpCode = otpCodeOpt.get();

        if (otpCode.isExpired()) {
            log.warn("OTP expired for email: {}", email);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Increment attempts BEFORE checking the code so every wrong guess counts
        otpCode.setAttempts(otpCode.getAttempts() + 1);

        if (otpCode.getAttempts() > maxAttempts) {
            otpCodeRepository.save(otpCode);
            log.warn("Max OTP attempts exceeded for email: {}", email);
            throw new BadRequestException("Maximum attempts exceeded. Please request a new OTP.");
        }

        if (!otpCode.getCode().equals(code)) {
            otpCodeRepository.save(otpCode);
            log.warn("Wrong OTP code for email: {} (attempt {}/{})", email, otpCode.getAttempts(), maxAttempts);
            return false;
        }

        otpCode.setVerified(true);
        otpCode.setVerifiedAt(LocalDateTime.now());
        otpCodeRepository.save(otpCode);

        log.info("OTP verified for email: {} type: {}", email, type);
        return true;
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Expired OTPs cleaned up.");
    }
}
