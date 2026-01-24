package com.example.digital_donation_api.exception;
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
