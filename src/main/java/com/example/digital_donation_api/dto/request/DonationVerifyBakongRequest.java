package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DonationVerifyBakongRequest {
    @NotNull
    private Long eventId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String md5Hash;

    private Boolean anonymous = false;
}
