package com.example.digital_donation_api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiError {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private java.util.List<FieldErrorDTO> errors;

    public ApiError(boolean success, int status, String error, String message, String path, LocalDateTime timestamp) {
        this.success = success;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }
}
