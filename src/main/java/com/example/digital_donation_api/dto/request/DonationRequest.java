package com.example.digital_donation_api.dto.request;

import com.example.digital_donation_api.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class DonationRequest {

    @NotNull
    private Long eventId;

    @NotNull
    @jakarta.validation.constraints.Min(1)
    private BigDecimal amount;

    private Boolean anonymous = false;
    
    @NotNull
    private PaymentMethod paymentMethod;
    
    private PaymentDetailsRequest paymentDetails;
}
