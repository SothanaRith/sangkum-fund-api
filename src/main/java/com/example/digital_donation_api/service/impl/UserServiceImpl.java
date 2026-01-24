package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    @Override
    public User updateProfile(Long id, String name, String avatar, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        
        return userRepository.save(user);
    }
}
