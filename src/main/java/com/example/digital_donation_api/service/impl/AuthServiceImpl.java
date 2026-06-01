package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.config.JwtConfig;
import com.example.digital_donation_api.dto.response.TokenResponse;
import com.example.digital_donation_api.dto.mapper.UserMapper;
import com.example.digital_donation_api.entity.OtpType;
import com.example.digital_donation_api.entity.RefreshToken;
import com.example.digital_donation_api.entity.Role;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.entity.UserRole;
import com.example.digital_donation_api.exception.BadRequestException;
import com.example.digital_donation_api.exception.UnauthorizedException;
import com.example.digital_donation_api.repository.RoleRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.repository.UserRoleRepository;
import com.example.digital_donation_api.service.AccountLockService;
import com.example.digital_donation_api.service.AuthService;
import com.example.digital_donation_api.service.EmailService;
import com.example.digital_donation_api.service.JwtService;
import com.example.digital_donation_api.service.OtpService;
import com.example.digital_donation_api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtConfig jwtConfig;
    private final OtpService otpService;
    private final EmailService emailService;
    private final AccountLockService accountLockService;

    // ── Login (password only) ─────────────────────────────────────────────────

    @Override
    public TokenResponse login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            checkNotBlocked(user);
            checkNotLocked(user);

            if (!passwordEncoder.matches(password, user.getPassword())) {
                accountLockService.incrementFailedAttempts(user);
                int remaining = accountLockService.getRemainingAttempts(user);
                if (remaining > 0) {
                    throw new UnauthorizedException(
                            "Invalid credentials. You have " + remaining + " attempt(s) remaining.");
                }
                throw new UnauthorizedException(
                        "Account locked due to too many failed attempts. Try again in 30 minutes.");
            }

            accountLockService.resetFailedAttempts(user);
            return buildTokenResponse(user);

        } catch (Exception e) {
            log.error("Login error for email: {}", email, e);
            throw e;
        }
    }

    // ── Login (password + OTP) ────────────────────────────────────────────────

    @Override
    public TokenResponse loginWithOtp(String email, String password, String otpCode) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            checkNotBlocked(user);
            checkNotLocked(user);

            if (!passwordEncoder.matches(password, user.getPassword())) {
                accountLockService.incrementFailedAttempts(user);
                int remaining = accountLockService.getRemainingAttempts(user);
                if (remaining > 0) {
                    throw new UnauthorizedException(
                            "Invalid credentials. You have " + remaining + " attempt(s) remaining.");
                }
                throw new UnauthorizedException(
                        "Account locked due to too many failed attempts. Try again in 30 minutes.");
            }

            if (!otpService.verifyOtp(email, otpCode, OtpType.LOGIN)) {
                throw new BadRequestException("Invalid or expired OTP code.");
            }

            accountLockService.resetFailedAttempts(user);
            return buildTokenResponse(user);

        } catch (Exception e) {
            log.error("Login with OTP error for email: {}", email, e);
            throw e;
        }
    }

    // ── Register (password only) ──────────────────────────────────────────────

    @Override
    public User register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        userRepository.save(user);

        assignRole(user, "DONOR");
        return user;
    }

    // ── Register with OTP ─────────────────────────────────────────────────────

    @Override
    public User registerWithOtp(String name, String email, String password, String otpCode) {
        if (!otpService.verifyOtp(email, otpCode, OtpType.REGISTRATION)) {
            throw new BadRequestException("Invalid or expired OTP code.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        userRepository.save(user);

        assignRole(user, "DONOR");
        emailService.sendWelcomeEmail(email, name);

        log.info("User registered via OTP: {}", email);
        return user;
    }

    // ── Token refresh (with rotation) ─────────────────────────────────────────

    @Override
    public TokenResponse refreshToken(String oldRefreshTokenStr) {
        RefreshToken oldToken = refreshTokenService.verifyRefreshToken(oldRefreshTokenStr);
        User user = oldToken.getUser();

        // Revoke old token and issue a brand-new refresh token
        refreshTokenService.revokeRefreshToken(oldRefreshTokenStr);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .user(UserMapper.toResponse(user))
                .build();
    }

    // ── Send OTPs ────────────────────────────────────────────────────────────

    @Override
    public void sendLoginOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with this email."));
        checkNotBlocked(user);
        checkNotLocked(user);
        otpService.generateAndSendOtp(email, OtpType.LOGIN);
    }

    @Override
    public void sendRegistrationOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists.");
        }
        otpService.generateAndSendOtp(email, OtpType.REGISTRATION);
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Override
    public void logout(String refreshTokenStr) {
        refreshTokenService.revokeRefreshToken(refreshTokenStr);
        log.info("User logged out, refresh token revoked.");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void checkNotBlocked(User user) {
        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            String reason = user.getBlockReason() != null ? user.getBlockReason() : "Policy violation";
            throw new UnauthorizedException("Your account has been blocked. Reason: " + reason);
        }
    }

    private void checkNotLocked(User user) {
        if (accountLockService.isAccountLocked(user)) {
            throw new UnauthorizedException(
                    "Account locked due to multiple failed login attempts. Try again in 30 minutes.");
        }
    }

    private TokenResponse buildTokenResponse(User user) {
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .user(UserMapper.toResponse(user))
                .build();
    }

    private void assignRole(User user, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return roleRepository.save(r);
                });
        UserRole ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepository.save(ur);
    }
}
