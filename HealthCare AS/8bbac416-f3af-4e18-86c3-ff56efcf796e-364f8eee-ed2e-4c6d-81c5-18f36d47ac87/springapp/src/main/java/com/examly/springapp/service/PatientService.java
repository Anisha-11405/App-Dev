package com.examly.springapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examly.springapp.model.Patient;
import com.examly.springapp.repository.PatientRepository;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    public Patient updatePatient(Long id, Patient updatedPatient) {
        Patient existing = patientRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(updatedPatient.getName());
            existing.setEmail(updatedPatient.getEmail());
            existing.setPhoneNumber(updatedPatient.getPhoneNumber());
            existing.setDateOfBirth(updatedPatient.getDateOfBirth());
            return patientRepository.save(existing);
        }
        return null;
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }

    public Long getPatientIdByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(Patient::getId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with email: " + email));
    }
}