package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.CharityStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.dto.response.CharityStatsResponse;
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
    
    @Override
    public void reject(Long charityId) {
        Charity charity = getById(charityId);
        charity.setStatus(CharityStatus.REJECTED);
    }

    @Override
    public void delete(Long charityId) {
        getById(charityId); // throws ResourceNotFoundException if missing
        charityRepository.deleteById(charityId);
    }

    @Override
    public CharityStatsResponse getCharityStats(Long charityId) {
        Charity charity = getById(charityId);
        
        // Calculate derived stats
        Long totalDonorsCount = 0L; // Would need a Donation service to calculate
        Long totalRaisedAmount = 0L; // Would need a Donation service to calculate
        Long totalEvents = 0L; // Would need an Event service to calculate
        Long activeEventsCount = 0L; // Would need an Event service to calculate
        Long activeProjects = 0L; // Would need a Project service to calculate
        String impactPercentage = "0%"; // Would need to calculate from events
        
        return CharityStatsResponse.builder()
                .charityId(charity.getId())
                .charityName(charity.getName())
                .totalDonations(charity.getTotalDonations())
                .beneficiariesCount(charity.getBeneficiariesCount())
                .volunteersCount(charity.getVolunteersCount())
                .yearsActive(charity.getYearsActive())
                .totalEvents(totalEvents)
                .activeEventsCount(activeEventsCount)
                .totalDonorsCount(totalDonorsCount)
                .totalRaisedAmount(totalRaisedAmount)
                .averageRating(charity.getRatingScore())
                .reviewCount(charity.getReviewCount())
                .activeProjects(activeProjects)
                .impactPercentage(impactPercentage)
                .build();
    }
    
    @Override
    public Page<Charity> searchCharities(String keyword, Pageable pageable) {
        return charityRepository.searchByKeyword(keyword, pageable);
    }
    
    @Override
    public Page<Charity> getByCategory(String category, Pageable pageable) {
        return charityRepository.findVerifiedByCategory(category, pageable);
    }
}
