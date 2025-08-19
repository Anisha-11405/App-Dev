package com.examly.springapp.dto;

import java.time.LocalDate;

import com.examly.springapp.model.Patient;

public record PatientDto(Long id, String name, String email, String phoneNumber, LocalDate dateOfBirth) {
   public static PatientDto from(Patient p){
     return new PatientDto(p.getId(), p.getName(), p.getEmail(), p.getPhoneNumber(), p.getDateOfBirth());
   }
}