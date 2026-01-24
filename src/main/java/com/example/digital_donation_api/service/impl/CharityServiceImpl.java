package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.CharityStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.CharityRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.CharityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CharityServiceImpl implements CharityService {

    private final CharityRepository charityRepository;
    private final UserRepository userRepository;

    @Override
    public Charity create(Charity charity, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        charity.setOwner(owner);
        charity.setStatus(CharityStatus.PENDING);
        return charityRepository.save(charity);
    }
    
    @Override
    public List<Charity> getAll() {
        return charityRepository.findAll();
    }
    
    @Override
    public List<Charity> getVerified() {
        return charityRepository.findByStatus(CharityStatus.VERIFIED);
    }
    
    @Override
    public Charity getById(Long id) {
        return charityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charity not found"));
    }

    @Override
    public void verify(Long charityId) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new ResourceNotFoundException("Charity not found"));
        charity.setStatus(CharityStatus.VERIFIED);
        charity.setVerifiedAt(LocalDateTime.now());
    }
}
