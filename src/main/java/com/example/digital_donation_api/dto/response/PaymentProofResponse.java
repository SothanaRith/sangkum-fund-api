package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PaymentProofResponse {
    private Long id;
    private Long donationId;
    private String imageUrl;
    private String notes;
    private String transactionId;
    private String bankName;
    private LocalDateTime transactionDate;
    private String status;
    private LocalDateTime uploadedAt;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private String adminNotes;
}
