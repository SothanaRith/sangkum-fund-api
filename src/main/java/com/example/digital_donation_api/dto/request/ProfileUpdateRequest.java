package com.example.digital_donation_api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    private String name;
    private String avatar;
    private String phone;
}
