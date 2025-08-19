package com.examly.springapp.model;

public enum AppointmentStatus {
    SCHEDULED,    // Initial status when booked (pending doctor approval)
    PENDING,      // Alternative to SCHEDULED
    CONFIRMED,    // Approved by doctor
    COMPLETED,    // Consultation completed
    CANCELLED,     // Rejected by doctor or cancelled
    APPROVED,
    REJECTED
}