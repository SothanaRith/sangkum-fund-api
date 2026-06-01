package com.example.digital_donation_api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(min=2, max=50)
    private String name;
    private String avatar;
    private String phone;
}
