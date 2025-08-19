package com.examly.springapp.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.model.Appointment;
import com.examly.springapp.model.AppointmentStatus;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.service.AppointmentService;
import com.examly.springapp.service.DoctorService;
import com.examly.springapp.service.PatientService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private PatientService patientService;

    // Get appointments for the logged-in doctor
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/my-appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(Authentication authentication) {
        try {
            String email = authentication.getName();
            Doctor doctor = doctorService.getDoctorByEmail(email);
            
            if (doctor == null) {
                return ResponseEntity.status(404).body(null);
            }
            
            List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            System.err.println("Error fetching doctor appointments: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // Confirm appointment
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<String> confirmAppointment(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            String result = appointmentService.confirmAppointment(id, email, isAdmin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to confirm appointment: " + e.getMessage());
        }
    }

    // Complete appointment
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> completeAppointment(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            String result = appointmentService.completeAppointment(id, email, isAdmin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to complete appointment: " + e.getMessage());
        }
    }
    
    // View all appointments
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getAllAppointments();
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", "Appointment not found with ID: " + id));
        }
    }
    
    // Book new appointment - FIXED VERSION
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @PostMapping
    public ResponseEntity<?> bookAppointment(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            String email = authentication.getName();
            System.out.println("🔑 Booking appointment for user: " + email);
            
            // Debug: Print all authorities
            System.out.println("🔐 User authorities: " + authentication.getAuthorities());
            
            // Check if user has required role
            boolean isPatient = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"));
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            System.out.println("👤 Is Patient: " + isPatient + ", Is Admin: " + isAdmin);
            
            if (!isPatient && !isAdmin) {
                return ResponseEntity.status(403).body(Map.of("error", "Access Denied: Insufficient permissions to book appointments"));
            }
            
            Long patientId;
            
            // Determine patientId based on user role
            if (isAdmin && body.containsKey("patientId") && body.get("patientId") != null) {
                // Admin can specify any patient
                patientId = Long.parseLong(body.get("patientId").toString());
                System.out.println("📋 Admin booking for patient ID: " + patientId);
            } else if (isPatient) {
                // Patient can only book for themselves
                try {
                    patientId = patientService.getPatientIdByEmail(email);
                    System.out.println("✅ Patient ID found: " + patientId);
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Patient not found for email: " + email);
                    return ResponseEntity.status(403).body(Map.of("error", "Access Denied: Patient not found with email: " + email));
                }
            } else {
                return ResponseEntity.status(400).body(Map.of("error", "Patient ID is required for admin users"));
            }

            // Validate required fields
            if (!body.containsKey("doctorId") || body.get("doctorId") == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Doctor ID is required"));
            }
            if (!body.containsKey("appointmentDate") || body.get("appointmentDate") == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Appointment date is required"));
            }
            if (!body.containsKey("appointmentTime") || body.get("appointmentTime") == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Appointment time is required"));
            }
            if (!body.containsKey("reason") || body.get("reason") == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Reason is required"));
            }

            Long doctorId = Long.parseLong(body.get("doctorId").toString());
            LocalDate date = LocalDate.parse(body.get("appointmentDate").toString());
            LocalTime time = LocalTime.parse(body.get("appointmentTime").toString());
            String reason = body.get("reason").toString();

            System.out.println("📅 Booking details - Patient: " + patientId + ", Doctor: " + doctorId + ", Date: " + date + ", Time: " + time);

            Appointment appointment = appointmentService.bookAppointment(patientId, doctorId, date, time, reason);
            System.out.println("✅ Appointment booked successfully with ID: " + appointment.getId());
            
            return ResponseEntity.status(201).body(appointment);
            
        } catch (IllegalStateException e) {
            System.out.println("❌ Booking conflict: " + e.getMessage());
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid argument: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Failed to book appointment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to book appointment: " + e.getMessage()));
        }
    }
    
    // Approve/Reject appointments
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            AppointmentStatus status = AppointmentStatus.valueOf(body.get("status"));
            Appointment updated = appointmentService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
    
    // View appointments by patient
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @GetMapping("/patient/{id}")
    public ResponseEntity<?> getByPatient(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            List<Appointment> appointments = appointmentService.getByPatientId(id);
            return ResponseEntity.ok(appointments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
    
    // View appointments by doctor
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @GetMapping("/doctor/{id}")
    public ResponseEntity<?> getByDoctor(@PathVariable Long id) {
        try {
            List<Appointment> appointments = appointmentService.getByDoctorId(id);
            return ResponseEntity.ok(appointments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cancel appointment
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAppointmentByAuth(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            String result = appointmentService.cancelAppointment(id, email, isAdmin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to cancel appointment: " + e.getMessage());
        }
    }
    
    // Delete appointment
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}