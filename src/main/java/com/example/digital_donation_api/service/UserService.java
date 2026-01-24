package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.User;

public interface UserService {
    User getById(Long id);
    
    User updateProfile(Long id, String name, String avatar, String phone);
}
