package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.CharityResponse;
import com.example.digital_donation_api.entity.Charity;

public class CharityMapper {
    public static CharityResponse toResponse(Charity charity) {
        return new CharityResponse(
                charity.getId(),
                charity.getName(),
                charity.getDescription(),
                charity.getLogo(),
                charity.getRegistrationNumber(),
                charity.getAddress(),
                charity.getContactEmail(),
                charity.getContactPhone(),
                charity.getWebsite(),
                charity.getCategory(),
                charity.getStatus().name(),
                charity.getVerifiedAt()
        );
    }
}
