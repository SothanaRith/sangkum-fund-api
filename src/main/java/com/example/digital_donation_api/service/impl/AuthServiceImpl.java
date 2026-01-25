package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.config.JwtConfig;
import com.example.digital_donation_api.dto.response.TokenResponse;
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

    @Override
    public TokenResponse login(String email, String password) {
        try {
            log.debug("Login attempt for email: {}", email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            // Check if user is blocked
            if (user.getIsBlocked()) {
                throw new UnauthorizedException(
                    "Your account has been blocked. Reason: " + user.getBlockReason()
                );
            }

            // Check if account is locked
            if (accountLockService.isAccountLocked(user)) {
                throw new UnauthorizedException(
                    "Account is locked due to multiple failed login attempts. Please try again later or contact support."
                );
            }

            log.debug("User found: {}, checking password", user.getEmail());
            if (!passwordEncoder.matches(password, user.getPassword())) {
                // Increment failed attempts
                accountLockService.incrementFailedAttempts(user);
                int remainingAttempts = accountLockService.getRemainingAttempts(user);
                
                if (remainingAttempts > 0) {
                    throw new UnauthorizedException(
                        "Invalid credentials. You have " + remainingAttempts + " attempt(s) remaining."
                    );
                } else {
                    throw new UnauthorizedException(
                        "Account has been locked due to too many failed login attempts. Please try again in 30 minutes."
                    );
                }
            }

            // Reset failed attempts on successful login
            accountLockService.resetFailedAttempts(user);

            log.debug("Password matches, generating tokens");
            String accessToken = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            
            log.debug("Tokens generated successfully");
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtConfig.getExpiration() / 1000) // Convert to seconds
                    .build();
        } catch (Exception e) {
            log.error("Login error for email: {}", email, e);
            throw e;
        }
    }

    @Override
    public User register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        user.setIsActive(true);
        userRepository.save(user);

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

        return user;
    }

    @Override
    public TokenResponse refreshToken(String refreshTokenStr) {
        log.debug("Refreshing token");
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();
        
        String newAccessToken = jwtService.generateToken(user);
        
        log.debug("Token refreshed successfully");
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .build();
    }

    @Override
    public TokenResponse loginWithOtp(String email, String password, String otpCode) {
        try {
            log.debug("Login with OTP attempt for email: {}", email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            // Check if account is locked
            if (accountLockService.isAccountLocked(user)) {
                throw new UnauthorizedException(
                    "Account is locked due to multiple failed login attempts. Please try again later or contact support."
                );
            }

            // Verify credentials first
            if (!passwordEncoder.matches(password, user.getPassword())) {
                // Increment failed attempts
                accountLockService.incrementFailedAttempts(user);
                int remainingAttempts = accountLockService.getRemainingAttempts(user);
                
                if (remainingAttempts > 0) {
                    throw new UnauthorizedException(
                        "Invalid credentials. You have " + remainingAttempts + " attempt(s) remaining."
                    );
                } else {
                    throw new UnauthorizedException(
                        "Account has been locked due to too many failed login attempts. Please try again in 30 minutes."
                    );
                }
            }
            
            // Verify OTP
            boolean otpValid = otpService.verifyOtp(email, otpCode, OtpType.LOGIN);
            if (!otpValid) {
                throw new BadRequestException("Invalid OTP code");
            }

            // Reset failed attempts on successful login
            accountLockService.resetFailedAttempts(user);

            log.debug("Password and OTP verified successfully, generating tokens");
            String accessToken = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            
            log.debug("Tokens generated successfully");
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtConfig.getExpiration() / 1000)
                    .build();
        } catch (Exception e) {
            log.error("Login with OTP error for email: {}", email, e);
            throw e;
        }
    }

    @Override
    public User registerWithOtp(String name, String email, String password, String otpCode) {
        log.debug("Register with OTP attempt for email: {}", email);
        
        // Verify OTP first
        boolean otpValid = otpService.verifyOtp(email, otpCode, OtpType.REGISTRATION);
        if (!otpValid) {
            throw new BadRequestException("Invalid OTP code");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        userRepository.save(user);

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
        
        // Send welcome email
        emailService.sendWelcomeEmail(email, name);

        log.debug("User registered successfully with email: {}", email);
        return user;
    }

    @Override
    public String sendLoginOtp(String email) {
        log.debug("Sending login OTP to email: {}", email);
        
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User with this email does not exist"));
        
        // Check if account is locked
        if (accountLockService.isAccountLocked(user)) {
            throw new UnauthorizedException(
                "Account is locked due to multiple failed login attempts. Please try again later or contact support."
            );
        }
        
        return otpService.generateAndSendOtp(email, OtpType.LOGIN);
    }

    @Override
    public String sendRegistrationOtp(String email) {
        log.debug("Sending registration OTP to email: {}", email);
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }
        
        return otpService.generateAndSendOtp(email, OtpType.REGISTRATION);
    }

    @Override
    public void logout(String refreshTokenStr) {
        log.debug("Logging out user with refresh token");
        refreshTokenService.revokeRefreshToken(refreshTokenStr);
        log.debug("Refresh token revoked successfully");
    }
}
