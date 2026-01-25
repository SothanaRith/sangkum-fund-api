package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.AccountLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockServiceImpl implements AccountLockService {

    private final UserRepository userRepository;
    
    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    @Override
    @Transactional
    public void incrementFailedAttempts(User user) {
        int newFailedAttempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
        user.setFailedLoginAttempts(newFailedAttempts);
        
        log.warn("Failed login attempt {} of {} for user: {}", newFailedAttempts, maxLoginAttempts, user.getEmail());
        
        if (newFailedAttempts >= maxLoginAttempts) {
            lockAccount(user);
        } else {
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("Failed login attempts reset for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void lockAccount(User user) {
        user.setAccountLocked(true);
        user.setLockTime(LocalDateTime.now());
        userRepository.save(user);
        log.warn("Account locked for user: {} due to {} failed login attempts", 
                user.getEmail(), user.getFailedLoginAttempts());
    }

    @Override
    @Transactional
    public void unlockAccount(User user) {
        user.setAccountLocked(false);
        user.setLockTime(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("Account unlocked for user: {}", user.getEmail());
    }

    @Override
    public boolean isAccountLocked(User user) {
        if (user.getAccountLocked() != null && user.getAccountLocked()) {
            // Check if lock period has expired
            if (user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
                if (LocalDateTime.now().isAfter(unlockTime)) {
                    // Auto-unlock the account
                    unlockAccount(user);
                    return false;
                }
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public int getRemainingAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        return Math.max(0, maxLoginAttempts - attempts);
    }
}
