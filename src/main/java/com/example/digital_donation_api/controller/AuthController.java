package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.request.LoginRequest;
import com.example.digital_donation_api.dto.request.RegisterRequest;
import com.example.digital_donation_api.dto.response.TokenResponse;
import com.example.digital_donation_api.dto.response.UserResponse;
import com.example.digital_donation_api.dto.mapper.UserMapper;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }
}
