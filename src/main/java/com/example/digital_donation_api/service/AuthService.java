package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.User;

public interface AuthService {

    String login(String email, String password);

    User register(String name, String email, String password);
}
