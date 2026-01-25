package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.AccountLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AccountManagementController {

    private final AccountLockService accountLockService;
    private final UserRepository userRepository;

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<Map<String, String>> unlockAccount(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        accountLockService.unlockAccount(user);
        
        return ResponseEntity.ok(Map.of(
                "message", "Account unlocked successfully",
                "email", user.getEmail()
        ));
    }

    @PostMapping("/{userId}/lock")
    public ResponseEntity<Map<String, String>> lockAccount(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        accountLockService.lockAccount(user);
        
        return ResponseEntity.ok(Map.of(
                "message", "Account locked successfully",
                "email", user.getEmail()
        ));
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> getAccountStatus(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isLocked = accountLockService.isAccountLocked(user);
        int remainingAttempts = accountLockService.getRemainingAttempts(user);
        
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "isLocked", isLocked,
                "failedAttempts", user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0,
                "remainingAttempts", remainingAttempts,
                "lockTime", user.getLockTime() != null ? user.getLockTime().toString() : "N/A"
        ));
    }

    @PostMapping("/{userId}/reset-attempts")
    public ResponseEntity<Map<String, String>> resetFailedAttempts(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        accountLockService.resetFailedAttempts(user);
        
        return ResponseEntity.ok(Map.of(
                "message", "Failed login attempts reset successfully",
                "email", user.getEmail()
        ));
    }
}
