package com.examly.springapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.service.DoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@Valid @RequestBody Doctor doctor) {
        try {
            Doctor createdDoctor = doctorService.createDoctor(doctor);
            return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        try {
            System.out.println("Loading all doctors...");
            List<Doctor> doctors = doctorService.getAllDoctors();
            System.out.println("Found " + doctors.size() + " doctors");
            return new ResponseEntity<>(doctors, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error loading doctors: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            return doctor != null ? ResponseEntity.ok(doctor) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/docdelete/{id}")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long id) {
        try {
            String result = doctorService.deleteDoctor(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete doctor");
        }
    }

    
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        try {
            List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<String>> getAvailableSpecializations() {
        try {
            List<String> specializations = doctorService.getAvailableSpecializations();
            return ResponseEntity.ok(specializations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}