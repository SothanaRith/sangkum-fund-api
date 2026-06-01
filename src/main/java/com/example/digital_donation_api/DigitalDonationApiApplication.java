package com.example.digital_donation_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cache.annotation.EnableCaching
public class DigitalDonationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalDonationApiApplication.class, args);
    }

}
