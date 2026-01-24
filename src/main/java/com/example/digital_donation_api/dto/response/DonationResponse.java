package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class DonationResponse {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private Boolean anonymous;
    private String paymentMethod;
    private String transactionRef;
    private String status;
    private LocalDateTime createdAt;
}
