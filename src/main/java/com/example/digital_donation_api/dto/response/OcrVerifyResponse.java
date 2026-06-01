package com.example.digital_donation_api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OcrVerifyResponse {
    private boolean verified;
    private String message;
    private String extractedName;
    private String extractedId;
    private String confidence;
}
