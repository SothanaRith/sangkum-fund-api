package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.Charity;

import java.util.List;

public interface CharityService {

    Charity create(Charity charity, Long userId);
    
    List<Charity> getAll();
    
    List<Charity> getVerified();
    
    Charity getById(Long id);

    void verify(Long charityId);
}
