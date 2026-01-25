package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.request.LoginRequest;
import com.example.digital_donation_api.dto.request.LoginWithOtpRequest;
import com.example.digital_donation_api.dto.request.RefreshTokenRequest;
import com.example.digital_donation_api.dto.request.RegisterRequest;
import com.example.digital_donation_api.dto.request.RegisterWithOtpRequest;
import com.example.digital_donation_api.dto.request.SendOtpRequest;
import com.example.digital_donation_api.dto.response.TokenResponse;
import com.example.digital_donation_api.dto.response.UserResponse;
import com.example.digital_donation_api.dto.mapper.UserMapper;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PostMapping("/register/otp")
    public ResponseEntity<UserResponse> registerWithOtp(@Valid @RequestBody RegisterWithOtpRequest request) {
        User user = authService.registerWithOtp(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getOtpCode()
        );
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/login/otp")
    public ResponseEntity<TokenResponse> loginWithOtp(@Valid @RequestBody LoginWithOtpRequest request) {
        TokenResponse tokenResponse = authService.loginWithOtp(
                request.getEmail(),
                request.getPassword(),
                request.getOtpCode()
        );
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String purpose = request.getPurpose().toLowerCase();
        
        if ("registration".equals(purpose)) {
            authService.sendRegistrationOtp(request.getEmail());
        } else if ("login".equals(purpose)) {
            authService.sendLoginOtp(request.getEmail());
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid purpose. Use 'registration' or 'login'"));
        }
        
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully to " + request.getEmail(),
                "email", request.getEmail()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
