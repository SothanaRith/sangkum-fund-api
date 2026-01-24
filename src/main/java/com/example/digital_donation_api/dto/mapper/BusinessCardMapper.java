package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.BusinessCardResponse;
import com.example.digital_donation_api.entity.BusinessCard;

public class BusinessCardMapper {

    public static BusinessCardResponse toResponse(BusinessCard card) {
        String userAvatar = card.getUser().getAvatar();
        // Convert relative avatar URLs to absolute
        if (userAvatar != null && userAvatar.startsWith("/uploads/")) {
            userAvatar = "http://localhost:8080" + userAvatar;
        }
        
        BusinessCardResponse response = new BusinessCardResponse();
        response.setId(card.getId());
        response.setUserId(card.getUser().getId());
        response.setUserName(card.getUser().getName());
        response.setUserAvatar(userAvatar);
        response.setTemplate(card.getTemplate());
        response.setTitle(card.getTitle());
        response.setBio(card.getBio());
        response.setContactInfo(card.getContactInfo());
        response.setShareSlug(card.getShareSlug());
        response.setShareUrl("/card/" + card.getShareSlug());
        return response;
    }
}
