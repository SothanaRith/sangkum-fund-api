package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.request.PaymentDetailsRequest;
import com.example.digital_donation_api.entity.Donation;
import com.example.digital_donation_api.entity.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public interface DonationService {

    Donation donate(Long eventId, Long userId, BigDecimal amount, boolean anonymous, 
                   PaymentMethod paymentMethod, PaymentDetailsRequest paymentDetails);
                   
    List<Donation> getMyDonations(Long userId);
}
