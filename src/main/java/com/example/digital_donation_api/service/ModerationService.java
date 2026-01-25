package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.User;

public interface ModerationService {
    
    // User moderation
    User blockUser(Long userId, Long adminId, String reason);
    User unblockUser(Long userId, Long adminId);
    boolean isUserBlocked(Long userId);
    
    // Event moderation
    Event blockEvent(Long eventId, Long adminId, String reason);
    Event unblockEvent(Long eventId, Long adminId);
    
    // Charity moderation
    Charity blockCharity(Long charityId, Long adminId, String reason);
    Charity unblockCharity(Long charityId, Long adminId);
}
