package com.examly.springapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Doctor.ProfileStatus;
import com.examly.springapp.model.DoctorAvailability;
import com.examly.springapp.repository.DoctorAvailabilityRepository;
import com.examly.springapp.repository.DoctorRepository;

@Service
@Transactional
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // =============================================================================
    // FR2: Admin Features for Doctor Profile Management
    // =============================================================================

    /**
     * Admin creates new doctor profile with all required fields
     */
    public Doctor createDoctorProfile(CreateDoctorProfileRequest request) {
        // Check if email already exists
        if (existsByEmail(request.getEmail())) {
            throw new RuntimeException("Doctor with email " + request.getEmail() + " already exists");
        }

        Doctor doctor = Doctor.builder()
                .name(request.getName())
                .specialization(request.getSpecialization())
                .clinicName(request.getClinicName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .bio(request.getBio())
                .experienceYears(request.getExperienceYears())
                .qualifications(request.getQualifications())
                .consultationFee(request.getConsultationFee())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(ProfileStatus.ACTIVE)
                .userId(request.getUserId()) // Link to existing user account if provided
                .build();

        return doctorRepository.save(doctor);
    }

    /**
     * Admin updates existing doctor profile
     */
    public Doctor updateDoctorProfile(Long id, UpdateDoctorProfileRequest request) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            existingDoctor.setName(request.getName());
        }
        if (request.getSpecialization() != null) {
            existingDoctor.setSpecialization(request.getSpecialization());
        }
        if (request.getClinicName() != null) {
            existingDoctor.setClinicName(request.getClinicName());
        }
        if (request.getPhoneNumber() != null) {
            existingDoctor.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            existingDoctor.setAddress(request.getAddress());
        }
        if (request.getBio() != null) {
            existingDoctor.setBio(request.getBio());
        }
        if (request.getExperienceYears() != null) {
            existingDoctor.setExperienceYears(request.getExperienceYears());
        }
        if (request.getQualifications() != null) {
            existingDoctor.setQualifications(request.getQualifications());
        }
        if (request.getConsultationFee() != null) {
            existingDoctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getStatus() != null) {
            existingDoctor.setStatus(request.getStatus());
        }
        if (request.getUserId() != null) {
            existingDoctor.setUserId(request.getUserId());
        }

        return doctorRepository.save(existingDoctor);
    }

    /**
     * Admin links doctor profile to existing user account
     */
    public Doctor linkDoctorToUser(Long doctorId, Long userId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
        
        doctor.setUserId(userId);
        return doctorRepository.save(doctor);
    }

    /**
     * Admin gets all doctor profiles with filtering options
     */
    public List<Doctor> getAllDoctorProfiles(DoctorFilterRequest filter) {
        List<Doctor> doctors = doctorRepository.findAll();
        
        if (filter.getSpecialization() != null) {
            doctors = doctors.stream()
                    .filter(doc -> doc.getSpecialization().toLowerCase()
                            .contains(filter.getSpecialization().toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (filter.getStatus() != null) {
            doctors = doctors.stream()
                    .filter(doc -> doc.getStatus() == filter.getStatus())
                    .collect(Collectors.toList());
        }
        
        if (filter.getClinicName() != null) {
            doctors = doctors.stream()
                    .filter(doc -> doc.getClinicName() != null && 
                            doc.getClinicName().toLowerCase()
                            .contains(filter.getClinicName().toLowerCase()))
                    .collect(Collectors.toList());
        }

        return doctors;
    }

    // =============================================================================
    // FR2: Doctor Features for Own Profile Management
    // =============================================================================

    /**
     * Doctor views their own profile
     */
    public Doctor getMyProfile(String email) {
        return doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for email: " + email));
    }

    /**
     * Doctor updates their own profile information
     */
    public Doctor updateMyProfile(String email, UpdateMyProfileRequest request) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for email: " + email));

        // Doctors can update most fields except role, status, and userId
        if (request.getName() != null) {
            doctor.setName(request.getName());
        }
        if (request.getClinicName() != null) {
            doctor.setClinicName(request.getClinicName());
        }
        if (request.getPhoneNumber() != null) {
            doctor.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            doctor.setAddress(request.getAddress());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getExperienceYears() != null) {
            doctor.setExperienceYears(request.getExperienceYears());
        }
        if (request.getQualifications() != null) {
            doctor.setQualifications(request.getQualifications());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }

        return doctorRepository.save(doctor);
    }

    // =============================================================================
    // Existing methods from your current service
    // =============================================================================

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

    public Doctor getDoctorByEmail(String email) {
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
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
                Doctor doctor = doctorRepository.findById(id).orElse(null);
                if (doctor != null) {
                    availabilityRepository.deleteByDoctor(doctor);
                }
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

    public boolean isDoctorOwner(Long doctorId, String email) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        return doctor.isPresent() && doctor.get().getEmail().equals(email);
    }

    // Availability methods remain the same...
    @Transactional
    public String setDoctorAvailability(Long doctorId, AvailabilityRequest availabilityRequest) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

            availabilityRepository.deleteByDoctor(doctor);
            
            if (availabilityRequest.getAvailability() != null) {
                for (AvailabilityRequest.AvailabilitySlot slot : availabilityRequest.getAvailability()) {
                    if (slot.isAvailable() && slot.getTimeSlots() != null && !slot.getTimeSlots().isEmpty()) {
                        DoctorAvailability availability = new DoctorAvailability();
                        availability.setDoctor(doctor);
                        availability.setDayOfWeek(slot.getDay().toUpperCase());
                        availability.setTimeSlots(slot.getTimeSlots());
                        availability.setIsActive(true);
                        
                        availabilityRepository.save(availability);
                    }
                }
            }
            
            return "Availability updated successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to update availability: " + e.getMessage());
        }
    }

    public List<DoctorAvailability> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findActiveAvailabilityByDoctorId(doctorId);
    }

    // =============================================================================
    // DTOs for FR2 Implementation
    // =============================================================================

    public static class CreateDoctorProfileRequest {
        private String name;
        private String specialization;
        private String clinicName;
        private String email;
        private String phoneNumber;
        private String address;
        private String bio;
        private Integer experienceYears;
        private String qualifications;
        private Double consultationFee;
        private String password;
        private Long userId; // Link to existing user account

        // Constructors, getters, and setters
        public CreateDoctorProfileRequest() {}

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        
        public String getClinicName() { return clinicName; }
        public void setClinicName(String clinicName) { this.clinicName = clinicName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
        
        public String getQualifications() { return qualifications; }
        public void setQualifications(String qualifications) { this.qualifications = qualifications; }
        
        public Double getConsultationFee() { return consultationFee; }
        public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static class UpdateDoctorProfileRequest {
        private String name;
        private String specialization;
        private String clinicName;
        private String phoneNumber;
        private String address;
        private String bio;
        private Integer experienceYears;
        private String qualifications;
        private Double consultationFee;
        private ProfileStatus status;
        private Long userId;

        // Constructors, getters, and setters
        public UpdateDoctorProfileRequest() {}

        // Getters and setters (similar pattern as above)
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        
        public String getClinicName() { return clinicName; }
        public void setClinicName(String clinicName) { this.clinicName = clinicName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
        
        public String getQualifications() { return qualifications; }
        public void setQualifications(String qualifications) { this.qualifications = qualifications; }
        
        public Double getConsultationFee() { return consultationFee; }
        public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
        
        public ProfileStatus getStatus() { return status; }
        public void setStatus(ProfileStatus status) { this.status = status; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static class UpdateMyProfileRequest {
        private String name;
        private String clinicName;
        private String phoneNumber;
        private String address;
        private String bio;
        private Integer experienceYears;
        private String qualifications;
        private Double consultationFee;

        // Constructors, getters, and setters
        public UpdateMyProfileRequest() {}

        // Getters and setters (similar pattern)
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getClinicName() { return clinicName; }
        public void setClinicName(String clinicName) { this.clinicName = clinicName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
        
        public String getQualifications() { return qualifications; }
        public void setQualifications(String qualifications) { this.qualifications = qualifications; }
        
        public Double getConsultationFee() { return consultationFee; }
        public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    }

    public static class DoctorFilterRequest {
        private String specialization;
        private ProfileStatus status;
        private String clinicName;

        // Constructors, getters, and setters
        public DoctorFilterRequest() {}

        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        
        public ProfileStatus getStatus() { return status; }
        public void setStatus(ProfileStatus status) { this.status = status; }
        
        public String getClinicName() { return clinicName; }
        public void setClinicName(String clinicName) { this.clinicName = clinicName; }
    }

    // Existing DTO classes remain the same...
    public static class AvailabilityRequest {
        private Long doctorId;
        private List<AvailabilitySlot> availability;

        public AvailabilityRequest() {}
        
        public AvailabilityRequest(Long doctorId, List<AvailabilitySlot> availability) {
            this.doctorId = doctorId;
            this.availability = availability;
        }

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        
        public List<AvailabilitySlot> getAvailability() { return availability; }
        public void setAvailability(List<AvailabilitySlot> availability) { this.availability = availability; }

        public static class AvailabilitySlot {
            private String day;
            private String dayLabel;
            private List<String> timeSlots;
            private boolean isAvailable;

            public AvailabilitySlot() {}
            
            public AvailabilitySlot(String day, String dayLabel, List<String> timeSlots, boolean isAvailable) {
                this.day = day;
                this.dayLabel = dayLabel;
                this.timeSlots = timeSlots;
                this.isAvailable = isAvailable;
            }

            public String getDay() { return day; }
            public void setDay(String day) { this.day = day; }
            
            public String getDayLabel() { return dayLabel; }
            public void setDayLabel(String dayLabel) { this.dayLabel = dayLabel; }
            
            public List<String> getTimeSlots() { return timeSlots; }
            public void setTimeSlots(List<String> timeSlots) { this.timeSlots = timeSlots; }
            
            public boolean isAvailable() { return isAvailable; }
            public void setAvailable(boolean available) { isAvailable = available; }
        }
    }
}