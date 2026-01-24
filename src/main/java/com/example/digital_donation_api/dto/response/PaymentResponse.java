package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {
    private Long donationId;
    private String status;
    private String paymentMethod;
    private String transactionRef;
    private String qrCodeData; // For KHQR or OFFLINE_QR
    private String message;
}
