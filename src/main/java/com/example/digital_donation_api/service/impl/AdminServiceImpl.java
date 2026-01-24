package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.AdminService;
import com.example.digital_donation_api.service.CharityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final CharityService charityService;
    private final UserRepository userRepository;

    @Override
    public void verifyCharity(Long charityId) {
        charityService.verify(charityId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
