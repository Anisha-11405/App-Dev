package com.examly.springapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.examly.springapp.dto.AuthRequest;
import com.examly.springapp.dto.AuthResponse;
import com.examly.springapp.model.Admin;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.JwtUtil;
import com.examly.springapp.model.Patient;
import com.examly.springapp.model.Role;
import com.examly.springapp.repository.AdminRepository;
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
    private final AdminRepository adminRepository;

    public AuthController(AuthenticationManager authManager,
                          com.examly.springapp.service.MyUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          AdminRepository adminRepository) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> registrationData) {
        try {
            String name = (String) registrationData.get("name");
            String email = (String) registrationData.get("email");
            String password = (String) registrationData.get("password");
            String role = (String) registrationData.get("role");

            // Check if email already exists in any repository
            if (patientRepository.findByEmail(email).isPresent() ||
                doctorRepository.findByEmail(email).isPresent() ||
                adminRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(400).body("Email already exists");
            }

            // Encode password
            String encodedPassword = passwordEncoder.encode(password);

            if ("PATIENT".equals(role)) {
                String phoneNumber = (String) registrationData.get("phoneNumber");
                String dateOfBirth = (String) registrationData.get("dateOfBirth");

                // Validate required patient fields
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
                
                // Parse and set date of birth
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

            } else if ("ADMIN".equals(role)) {
                String phoneNumber = (String) registrationData.get("phoneNumber");
                
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    return ResponseEntity.status(400).body("Phone number is required for admins");
                }
                
                Admin admin = new Admin();
                admin.setName(name);
                admin.setEmail(email);
                admin.setPassword(encodedPassword);
                admin.setRole(Role.ADMIN);
                admin.setPhoneNumber(phoneNumber);

                adminRepository.save(admin);
                return ResponseEntity.ok("Admin registered successfully");

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
            System.out.println("🔐 Login attempt for: " + request.getEmail());
            
            // Authenticate user
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Fetch user from repositories
            Object user = patientRepository.findByEmail(request.getEmail())
                    .map(p -> (Object) p)
                    .orElseGet(() -> doctorRepository.findByEmail(request.getEmail())
                            .map(d -> (Object) d)
                            .orElseGet(() -> adminRepository.findByEmail(request.getEmail())
                                    .orElseThrow(() -> new RuntimeException("User not found"))));

            // Determine role and email
            Role role;
            String email;
            Long userId;
            String userName;
            
            if (user instanceof Patient patient) {
                role = patient.getRole();
                email = patient.getEmail();
                userId = patient.getId();
                userName = patient.getName();
            } else if (user instanceof Doctor doctor) {
                role = doctor.getRole();
                email = doctor.getEmail();
                userId = doctor.getId();
                userName = doctor.getName();
            } else if (user instanceof Admin admin) {
                role = admin.getRole();
                email = admin.getEmail();
                userId = admin.getId();
                userName = admin.getName();
            } else {
                throw new RuntimeException("Invalid user type");
            }

            // Generate JWT token with additional user info
            String token = jwtUtil.generateToken(email, role);
            
            System.out.println("✅ Login successful for: " + email + " with role: " + role);
            System.out.println("🎫 Generated token: " + token.substring(0, 20) + "...");

            // Return comprehensive auth response
            Map<String, Object> response = Map.of(
                "token", token,
                "user", Map.of(
                    "id", userId,
                    "name", userName,
                    "email", email,
                    "role", role.name()
                )
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.out.println("❌ Invalid credentials for: " + request.getEmail());
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (DisabledException e) {
            System.out.println("❌ Account disabled for: " + request.getEmail());
            return ResponseEntity.status(401).body("Account disabled");
        } catch (Exception e) {
            System.out.println("❌ Authentication failed: " + e.getMessage());
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Note: With JWT, logout is typically handled client-side by removing the token
        // You could implement token blacklisting here if needed
        return ResponseEntity.ok("Logged out successfully");
    }

    // Get current user information
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            
            Object user = patientRepository.findByEmail(email)
                    .map(p -> (Object) p)
                    .orElseGet(() -> doctorRepository.findByEmail(email)
                            .map(d -> (Object) d)
                            .orElseGet(() -> adminRepository.findByEmail(email)
                                    .orElse(null)));

            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            if (user instanceof Patient patient) {
                return ResponseEntity.ok(Map.of(
                    "id", patient.getId(),
                    "name", patient.getName(),
                    "email", patient.getEmail(),
                    "role", patient.getRole().name(),
                    "phoneNumber", patient.getPhoneNumber(),
                    "dateOfBirth", patient.getDateOfBirth()
                ));
            } else if (user instanceof Doctor doctor) {
                return ResponseEntity.ok(Map.of(
                    "id", doctor.getId(),
                    "name", doctor.getName(),
                    "email", doctor.getEmail(),
                    "role", doctor.getRole().name(),
                    "phoneNumber", doctor.getPhoneNumber(),
                    "specialization", doctor.getSpecialization()
                ));
            } else if (user instanceof Admin admin) {
                return ResponseEntity.ok(Map.of(
                    "id", admin.getId(),
                    "name", admin.getName(),
                    "email", admin.getEmail(),
                    "role", admin.getRole().name(),
                    "phoneNumber", admin.getPhoneNumber()
                ));
            }

            return ResponseEntity.status(500).body("Unknown user type");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get current user: " + e.getMessage());
        }
    }

    @GetMapping("/test")
public ResponseEntity<String> test() {
    return ResponseEntity.ok("AuthController is working!");
}
}