package com.examly.springapp.service;

import com.examly.springapp.model.Patient;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.repository.PatientRepository;
import com.examly.springapp.repository.DoctorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public MyUserDetailsService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Doctor doctor = doctorRepository.findByEmail(email).orElse(null);
        if (doctor != null) {
            return new User(
                    doctor.getEmail(),
                    doctor.getPassword(),
                    Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_DOCTOR")
                    )
            );
        }

        Patient patient = patientRepository.findByEmail(email).orElse(null);
        if (patient != null) {
            return new User(
                    patient.getEmail(),
                    patient.getPassword(),
                    Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_PATIENT")
                    )
            );
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }
}
