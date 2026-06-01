package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.DonationResponse;
import com.example.digital_donation_api.entity.Donation;

public class DonationMapper {
    public static DonationResponse toResponse(Donation donation) {
        String donorName = null;
        if (!Boolean.TRUE.equals(donation.getIsAnonymous()) && donation.getUser() != null) {
            donorName = donation.getUser().getName();
        }
        Long eventId = donation.getEvent() != null ? donation.getEvent().getId() : null;
        String eventTitle = donation.getEvent() != null ? donation.getEvent().getTitle() : null;

        return new DonationResponse(
                donation.getId(),
                donation.getAmount(),
                donation.getCurrency(),
                donation.getIsAnonymous(),
                donation.getPaymentMethod() != null ? donation.getPaymentMethod().name() : null,
                donation.getTransactionRef(),
                donation.getStatus().name(),
                donation.getCreatedAt(),
                donorName,
                eventId,
                eventTitle
        );
    }
}
