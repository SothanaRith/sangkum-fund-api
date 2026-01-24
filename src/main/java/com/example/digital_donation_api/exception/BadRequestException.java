package com.example.digital_donation_api.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
