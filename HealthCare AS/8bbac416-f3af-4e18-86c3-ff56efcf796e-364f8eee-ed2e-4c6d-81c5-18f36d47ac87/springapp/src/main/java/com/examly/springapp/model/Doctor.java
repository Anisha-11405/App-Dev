package com.examly.springapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be 3-50 characters")
    private String name;
    
    @NotBlank(message = "Specialization is required")
    private String specialization;
    
    // NEW: Clinic name field for FR2
    @Size(max = 100, message = "Clinic name cannot exceed 100 characters")
    private String clinicName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    // NEW: Additional contact info for FR2
    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;
    
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    // NEW: Experience and qualifications
    private Integer experienceYears;
    
    @Size(max = 300, message = "Qualifications cannot exceed 300 characters")
    private String qualifications;
    
    // NEW: Consultation fee
    private Double consultationFee;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.DOCTOR;
    
    // NEW: Profile status for admin management
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status = ProfileStatus.ACTIVE;
    
    // NEW: Link to user account
    @Column(name = "user_id")
    private Long userId; // Links to User entity if you have one
    
    public enum ProfileStatus {
        ACTIVE,
        INACTIVE,
        PENDING,
        SUSPENDED
    }
}