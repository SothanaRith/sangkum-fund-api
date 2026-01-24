package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String avatar;
    private String phone;
    private Boolean active;
}
