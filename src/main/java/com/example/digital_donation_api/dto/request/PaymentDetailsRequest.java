package com.example.digital_donation_api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetailsRequest {
    // For VISA_CARD
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    
    // For KHQR and OFFLINE_QR
    private String qrCode;
    private String phoneNumber;
    
    // Common
    private String notes;
}
