package com.example.digital_donation_api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class EventUpdateRequest {
    private String title;
    private String description;
    private BigDecimal goalAmount;
    private String status;
}
