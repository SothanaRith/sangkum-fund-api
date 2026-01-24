package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.User;
import java.util.List;

public interface AdminService {

    void verifyCharity(Long charityId);

    List<User> getAllUsers();
}

