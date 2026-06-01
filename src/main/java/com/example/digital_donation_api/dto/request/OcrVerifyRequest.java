package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OcrVerifyRequest {

    @NotBlank
    private String documentImage;

    @NotBlank
    private String documentType;

    private String expectedName;
}
