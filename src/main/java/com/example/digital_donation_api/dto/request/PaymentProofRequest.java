package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentProofRequest {

    @NotNull
    private Long donationId;
    
    @NotNull
    private String imageUrl; // URL or base64 encoded image
    
    private String notes;
    private String transactionId;
    private String bankName;
    private String transactionDate; // Format: yyyy-MM-dd HH:mm:ss
}
