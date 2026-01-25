package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.CharityStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.CharityRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.CharityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Charity update(Long charityId, Charity charityData, Long userId) {
        Charity charity = getById(charityId);
        
        // Verify ownership
        if (!charity.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("Only charity owner can update the charity");
        }
        
        // Update allowed fields
        if (charityData.getName() != null) {
            charity.setName(charityData.getName());
        }
        if (charityData.getDescription() != null) {
            charity.setDescription(charityData.getDescription());
        }
        if (charityData.getLogo() != null) {
            charity.setLogo(charityData.getLogo());
        }
        if (charityData.getRegistrationNumber() != null) {
            charity.setRegistrationNumber(charityData.getRegistrationNumber());
        }
        if (charityData.getAddress() != null) {
            charity.setAddress(charityData.getAddress());
        }
        if (charityData.getContactEmail() != null) {
            charity.setContactEmail(charityData.getContactEmail());
        }
        if (charityData.getContactPhone() != null) {
            charity.setContactPhone(charityData.getContactPhone());
        }
        if (charityData.getWebsite() != null) {
            charity.setWebsite(charityData.getWebsite());
        }
        if (charityData.getCategory() != null) {
            charity.setCategory(charityData.getCategory());
        }
        
        return charityRepository.save(charity);
    }
    
    @Override
    public Page<Charity> getAll(Pageable pageable) {
        return charityRepository.findAll(pageable);
    }
    
    @Override
    public Page<Charity> getVerified(Pageable pageable) {
        return charityRepository.findByStatus(CharityStatus.VERIFIED, pageable);
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
