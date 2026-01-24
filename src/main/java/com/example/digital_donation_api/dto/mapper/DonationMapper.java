package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.DonationResponse;
import com.example.digital_donation_api.entity.Donation;

public class DonationMapper {
    public static DonationResponse toResponse(Donation donation) {
        return new DonationResponse(
                donation.getId(),
                donation.getAmount(),
                donation.getCurrency(),
                donation.getIsAnonymous(),
                donation.getPaymentMethod() != null ? donation.getPaymentMethod().name() : null,
                donation.getTransactionRef(),
                donation.getStatus().name(),
                donation.getCreatedAt()
        );
    }
}
