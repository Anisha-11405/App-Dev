package com.examly.springapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examly.springapp.model.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {
        Optional<Doctor> findByEmail(String email);
}
