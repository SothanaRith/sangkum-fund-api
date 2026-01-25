package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.*;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.CharityRepository;
import com.example.digital_donation_api.repository.EventRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ModerationServiceImpl implements ModerationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CharityRepository charityRepository;

    @Override
    public User blockUser(Long userId, Long adminId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getIsBlocked()) {
            throw new IllegalStateException("User is already blocked");
        }
        
        user.setIsBlocked(true);
        user.setBlockReason(reason);
        user.setBlockedAt(LocalDateTime.now());
        user.setBlockedBy(adminId);
        user.setIsActive(false); // Also deactivate the account
        
        log.info("User {} blocked by admin {}: {}", userId, adminId, reason);
        return userRepository.save(user);
    }

    @Override
    public User unblockUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getIsBlocked()) {
            throw new IllegalStateException("User is not blocked");
        }
        
        user.setIsBlocked(false);
        user.setBlockReason(null);
        user.setBlockedAt(null);
        user.setBlockedBy(null);
        user.setIsActive(true); // Reactivate the account
        
        log.info("User {} unblocked by admin {}", userId, adminId);
        return userRepository.save(user);
    }

    @Override
    public boolean isUserBlocked(Long userId) {
        return userRepository.findById(userId)
                .map(User::getIsBlocked)
                .orElse(false);
    }

    @Override
    public Event blockEvent(Long eventId, Long adminId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (event.getStatus() == EventStatus.BLOCKED) {
            throw new IllegalStateException("Event is already blocked");
        }
        
        event.setStatus(EventStatus.BLOCKED);
        event.setRejectionReason(reason);
        event.setReviewedBy(adminId);
        event.setReviewedAt(LocalDateTime.now());
        
        log.info("Event {} blocked by admin {}: {}", eventId, adminId, reason);
        return eventRepository.save(event);
    }

    @Override
    public Event unblockEvent(Long eventId, Long adminId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (event.getStatus() != EventStatus.BLOCKED) {
            throw new IllegalStateException("Event is not blocked");
        }
        
        event.setStatus(EventStatus.APPROVED);
        event.setRejectionReason(null);
        event.setReviewedBy(adminId);
        event.setReviewedAt(LocalDateTime.now());
        
        log.info("Event {} unblocked by admin {}", eventId, adminId);
        return eventRepository.save(event);
    }

    @Override
    public Charity blockCharity(Long charityId, Long adminId, String reason) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new ResourceNotFoundException("Charity not found"));
        
        if (charity.getStatus() == CharityStatus.BLOCKED) {
            throw new IllegalStateException("Charity is already blocked");
        }
        
        charity.setStatus(CharityStatus.BLOCKED);
        // Could add reason field to Charity entity if needed
        
        log.info("Charity {} blocked by admin {}: {}", charityId, adminId, reason);
        return charityRepository.save(charity);
    }

    @Override
    public Charity unblockCharity(Long charityId, Long adminId) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new ResourceNotFoundException("Charity not found"));
        
        if (charity.getStatus() != CharityStatus.BLOCKED) {
            throw new IllegalStateException("Charity is not blocked");
        }
        
        charity.setStatus(CharityStatus.VERIFIED);
        
        log.info("Charity {} unblocked by admin {}", charityId, adminId);
        return charityRepository.save(charity);
    }
}
