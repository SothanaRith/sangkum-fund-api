package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.dto.response.CharityStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CharityService {

    Charity create(Charity charity, Long userId);
    
    Charity update(Long charityId, Charity charityData, Long userId);
    
    Page<Charity> getAll(Pageable pageable);
    
    Page<Charity> getVerified(Pageable pageable);
    
    Charity getById(Long id);

    void verify(Long charityId);
    
    // New impact and stats methods
    CharityStatsResponse getCharityStats(Long charityId);
    
    Page<Charity> searchCharities(String keyword, Pageable pageable);
    
    Page<Charity> getByCategory(String category, Pageable pageable);
}
