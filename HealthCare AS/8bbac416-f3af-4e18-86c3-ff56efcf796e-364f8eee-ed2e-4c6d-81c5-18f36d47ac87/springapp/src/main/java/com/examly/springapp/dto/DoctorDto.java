package com.examly.springapp.dto;

import com.examly.springapp.model.Doctor;

public record DoctorDto(Long id, String name, String specialization, String email, String phoneNumber) {
    
    public static DoctorDto from(Doctor d) {
        return new DoctorDto(
            d.getId(),
            d.getName(),
            d.getSpecialization(),
            d.getEmail(),
            d.getPhoneNumber()
        );
    }
}
