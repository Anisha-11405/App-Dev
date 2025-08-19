package com.examly.springapp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.examly.springapp.model.Admin;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Patient;
import com.examly.springapp.repository.AdminRepository;
import com.examly.springapp.repository.DoctorRepository;
import com.examly.springapp.repository.PatientRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;

    public MyUserDetailsService(PatientRepository patientRepository, 
                               DoctorRepository doctorRepository,
                               AdminRepository adminRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Loading user by email: " + email);

        // Check in Patient repository
        return patientRepository.findByEmail(email)
                .map(patient -> {
                    System.out.println("✅ Found patient: " + patient.getName() + " with role: " + patient.getRole());
                    return createUserDetails(patient.getEmail(), patient.getPassword(), patient.getRole().name());
                })
                // Check in Doctor repository if not found in Patient
                .orElseGet(() -> doctorRepository.findByEmail(email)
                        .map(doctor -> {
                            System.out.println("✅ Found doctor: " + doctor.getName() + " with role: " + doctor.getRole());
                            return createUserDetails(doctor.getEmail(), doctor.getPassword(), doctor.getRole().name());
                        })
                        // Check in Admin repository if not found in Doctor
                        .orElseGet(() -> adminRepository.findByEmail(email)
                                .map(admin -> {
                                    System.out.println("✅ Found admin: " + admin.getName() + " with role: " + admin.getRole());
                                    return createUserDetails(admin.getEmail(), admin.getPassword(), admin.getRole().name());
                                })
                                // Throw exception if not found in any repository
                                .orElseThrow(() -> {
                                    System.out.println("❌ User not found with email: " + email);
                                    return new UsernameNotFoundException("User not found with email: " + email);
                                }))
                );
    }

    private UserDetails createUserDetails(String email, String password, String role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        
        System.out.println("🔑 Creating UserDetails for: " + email + " with authority: ROLE_" + role);

        return new org.springframework.security.core.userdetails.User(
                email,
                password,
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }

    // Helper methods to get user information
    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email).orElse(null);
    }

    public Doctor getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email).orElse(null);
    }

    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email).orElse(null);
    }

    // Method to get user role by email
    public String getUserRole(String email) {
        return patientRepository.findByEmail(email)
                .map(patient -> patient.getRole().name())
                .orElseGet(() -> doctorRepository.findByEmail(email)
                        .map(doctor -> doctor.getRole().name())
                        .orElseGet(() -> adminRepository.findByEmail(email)
                                .map(admin -> admin.getRole().name())
                                .orElse(null)));
    }

    // Method to get user ID by email (useful for authorization checks)
    public Long getUserId(String email) {
        return patientRepository.findByEmail(email)
                .map(Patient::getId)
                .orElseGet(() -> doctorRepository.findByEmail(email)
                        .map(Doctor::getId)
                        .orElseGet(() -> adminRepository.findByEmail(email)
                                .map(Admin::getId)
                                .orElse(null)));
    }
}