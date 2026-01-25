package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.request.PaymentDetailsRequest;
import com.example.digital_donation_api.entity.Donation;
import com.example.digital_donation_api.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface DonationService {

    Donation donate(Long eventId, Long userId, BigDecimal amount, boolean anonymous, 
                   PaymentMethod paymentMethod, PaymentDetailsRequest paymentDetails);
                   
    Page<Donation> getMyDonations(Long userId, Pageable pageable);
}
