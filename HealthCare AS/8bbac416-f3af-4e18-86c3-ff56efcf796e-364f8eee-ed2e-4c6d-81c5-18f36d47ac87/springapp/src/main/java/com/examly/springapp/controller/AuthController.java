package com.examly.springapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import com.examly.springapp.dto.AuthRequest;
import com.examly.springapp.dto.AuthResponse;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.JwtUtil;
import com.examly.springapp.model.Patient;
import com.examly.springapp.model.Role;
import com.examly.springapp.repository.DoctorRepository;
import com.examly.springapp.repository.PatientRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final com.examly.springapp.service.MyUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AuthController(AuthenticationManager authManager,
                          com.examly.springapp.service.MyUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> registrationData) {
        try {
            String name = (String) registrationData.get("name");
            String email = (String) registrationData.get("email");
            String password = (String) registrationData.get("password");
            String role = (String) registrationData.get("role");

            if (patientRepository.findByEmail(email).isPresent() ||
                doctorRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(400).body("Email already exists");
            }

            String encodedPassword = passwordEncoder.encode(password);

            if ("PATIENT".equals(role)) {
                String phoneNumber = (String) registrationData.get("phoneNumber");
                String dateOfBirth = (String) registrationData.get("dateOfBirth");

                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    return ResponseEntity.status(400).body("Phone number is required for patients");
                }
                if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
                    return ResponseEntity.status(400).body("Date of birth is required for patients");
                }

                Patient patient = new Patient();
                patient.setName(name);
                patient.setEmail(email);
                patient.setPassword(encodedPassword);
                patient.setRole(Role.PATIENT);
                patient.setPhoneNumber(phoneNumber);
                
                try {
                    patient.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    return ResponseEntity.status(400).body("Invalid date format. Use YYYY-MM-DD");
                }

                patientRepository.save(patient);
                
                return ResponseEntity.ok("Patient registered successfully");

            } else if ("DOCTOR".equals(role)) {
                
                String phoneNumber = (String) registrationData.get("phoneNumber");
                String specialization = (String) registrationData.get("specialization");
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    return ResponseEntity.status(400).body("Phone number is required for doctors");
                }
                if (specialization == null || specialization.trim().isEmpty()) {
                    return ResponseEntity.status(400).body("Specialization is required for doctors");
                }
                Doctor doctor = new Doctor();

                doctor.setName(name);
                doctor.setEmail(email);
                doctor.setPassword(encodedPassword);
                doctor.setRole(Role.DOCTOR);
                doctor.setPhoneNumber(phoneNumber);
                doctor.setSpecialization(specialization);

                doctorRepository.save(doctor);
            
                return ResponseEntity.ok("Doctor registered successfully");

            } else {
                return ResponseEntity.status(400).body("Invalid role specified");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            System.out.println("Login attempt for: " + request.getEmail());
            
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Object user = patientRepository.findByEmail(request.getEmail())
                    .map(p -> (Object) p)
                    .orElseGet(() -> doctorRepository.findByEmail(request.getEmail())
                            .orElseThrow(() -> new RuntimeException("User not found")));

            Role role;
            String email;
            if (user instanceof Patient patient) {
                role = patient.getRole();
                email = patient.getEmail();
            } else if (user instanceof Doctor doctor) {
                role = doctor.getRole();
                email = doctor.getEmail();
            } else {
                throw new RuntimeException("Invalid user type");
            }

            String token = jwtUtil.generateToken(email, role);
            
            System.out.println("Login successful for: " + email + " with role: " + role);
            System.out.println("Generated token: " + token.substring(0, 20) + "...");

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (BadCredentialsException e) {
            System.out.println("Invalid credentials for: " + request.getEmail());
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (DisabledException e) {
            System.out.println("Account disabled for: " + request.getEmail());
            return ResponseEntity.status(401).body("Account disabled");
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}