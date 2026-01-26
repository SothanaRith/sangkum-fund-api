package com.example.digital_donation_api.entity;

public enum NotificationType {
    SYSTEM("System"),
    EVENT("Event"),
    DONATION("Donation"),
    USER("User"),
    SECURITY("Security"),
    ALERT("Alert"),
    INFO("Information");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
