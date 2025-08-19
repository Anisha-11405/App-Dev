package com.examly.springapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.repository.DoctorRepository;

@Service
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        return doctor.orElse(null);
    }

    public Doctor updateDoctor(Long id, Doctor doctor) {
        if (doctorRepository.existsById(id)) {
            doctor.setId(id);
            return doctorRepository.save(doctor);
        }
        return null;
    }

    public String deleteDoctor(Long id) {
        try {
            if (doctorRepository.existsById(id)) {
                doctorRepository.deleteById(id);
                return "Doctor deleted successfully";
            } else {
                return "Doctor not found with ID: " + id;
            }
        } catch (Exception e) {
            return "Failed to delete doctor: " + e.getMessage();
        }
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findAll().stream()
                .filter(doctor -> doctor.getSpecialization() != null && 
                         doctor.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<String> getAvailableSpecializations() {
        return doctorRepository.findAll().stream()
                .map(Doctor::getSpecialization)
                .filter(spec -> spec != null && !spec.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public long getTotalDoctorsCount() {
        return doctorRepository.count();
    }

    public boolean existsByEmail(String email) {
        return doctorRepository.findByEmail(email).isPresent();
    }
}