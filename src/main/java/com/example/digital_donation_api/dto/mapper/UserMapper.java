package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.UserResponse;
import com.example.digital_donation_api.entity.User;

public class UserMapper {
    public static UserResponse toResponse(User user) {
        String avatar = user.getAvatar();
        // Convert relative URLs to absolute URLs
        if (avatar != null && avatar.startsWith("/uploads/")) {
            avatar = "http://localhost:8080" + avatar;
        }
        
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                avatar,
                user.getPhone(),
                user.getIsActive()
        );
    }
}

