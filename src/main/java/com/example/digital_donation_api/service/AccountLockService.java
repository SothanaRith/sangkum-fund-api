package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.User;

public interface AccountLockService {
    
    void incrementFailedAttempts(User user);
    
    void resetFailedAttempts(User user);
    
    void lockAccount(User user);
    
    void unlockAccount(User user);
    
    boolean isAccountLocked(User user);
    
    int getRemainingAttempts(User user);
}
