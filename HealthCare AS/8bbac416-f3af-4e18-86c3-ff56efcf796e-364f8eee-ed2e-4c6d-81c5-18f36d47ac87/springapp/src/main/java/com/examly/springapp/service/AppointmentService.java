package com.examly.springapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examly.springapp.model.Appointment;
import com.examly.springapp.model.AppointmentStatus;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Patient;
import com.examly.springapp.repository.AppointmentRepository;
import com.examly.springapp.repository.DoctorRepository;
import com.examly.springapp.repository.PatientRepository;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;

    // Get appointments for specific doctor (for doctor role)
    public List<Appointment> getAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    // Get appointments by doctor email (for authenticated doctor)
    public List<Appointment> getMyDoctorAppointments(String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + doctorEmail));
        return appointmentRepository.findByDoctor(doctor);
    }

    // APPROVE appointment - Only doctors can approve their own appointments
    public String approveAppointment(Long appointmentId, String userEmail, boolean isAdmin) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return "Appointment not found";
            }

            Appointment appointment = appointmentOpt.get();
            
            // Check if user has permission to approve this appointment
            if (!isAdmin && !appointment.getDoctor().getEmail().equals(userEmail)) {
                return "You can only approve your own appointments";
            }

            if (appointment.getStatus() != AppointmentStatus.SCHEDULED && 
                appointment.getStatus() != AppointmentStatus.PENDING) {
                return "Only scheduled/pending appointments can be approved";
            }

            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
            return "Appointment approved successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to approve appointment: " + e.getMessage());
        }
    }

    // REJECT appointment - Only doctors can reject their own appointments
    public String rejectAppointment(Long appointmentId, String userEmail, boolean isAdmin, String rejectionReason) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return "Appointment not found";
            }

            Appointment appointment = appointmentOpt.get();
            
            // Check if user has permission to reject this appointment
            if (!isAdmin && !appointment.getDoctor().getEmail().equals(userEmail)) {
                return "You can only reject your own appointments";
            }

            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                return "Cannot reject a completed appointment";
            }

            appointment.setStatus(AppointmentStatus.CANCELLED);
            // You can add a rejection reason field to your Appointment model if needed
            appointmentRepository.save(appointment);
            return "Appointment rejected successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to reject appointment: " + e.getMessage());
        }
    }

    public String confirmAppointment(Long appointmentId, String userEmail, boolean isAdmin) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return "Appointment not found";
            }

            Appointment appointment = appointmentOpt.get();
            
            // Check if user has permission to confirm this appointment
            if (!isAdmin && !appointment.getDoctor().getEmail().equals(userEmail)) {
                return "You can only confirm your own appointments";
            }

            if (appointment.getStatus() != AppointmentStatus.PENDING && 
                appointment.getStatus() != AppointmentStatus.SCHEDULED) {
                return "Only pending/scheduled appointments can be confirmed";
            }

            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
            return "Appointment confirmed successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to confirm appointment: " + e.getMessage());
        }
    }

    public String cancelAppointment(Long appointmentId, String userEmail, boolean isAdmin) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return "Appointment not found";
            }

            Appointment appointment = appointmentOpt.get();
            
            // Check if user has permission to cancel this appointment
            boolean canCancel = isAdmin || 
                               appointment.getDoctor().getEmail().equals(userEmail) ||
                               appointment.getPatient().getEmail().equals(userEmail);
            
            if (!canCancel) {
                return "You don't have permission to cancel this appointment";
            }

            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                return "Cannot cancel a completed appointment";
            }

            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            return "Appointment cancelled successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel appointment: " + e.getMessage());
        }
    }

    public String completeAppointment(Long appointmentId, String userEmail, boolean isAdmin) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return "Appointment not found";
            }

            Appointment appointment = appointmentOpt.get();
            
            // Check if user has permission to complete this appointment
            if (!isAdmin && !appointment.getDoctor().getEmail().equals(userEmail)) {
                return "You can only complete your own appointments";
            }

            if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                return "Only confirmed appointments can be completed";
            }

            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
            return "Appointment completed successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to complete appointment: " + e.getMessage());
        }
    }
    
    public Appointment bookAppointment(Long patientId, Long doctorId, LocalDate date, LocalTime time, String reason) {
        if (patientId == null || doctorId == null || date == null || time == null || reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("All appointment details are required");
        }
        
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book appointment for past dates");
        }
        
        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Cannot book appointment for past time today");
        }
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));
        
        boolean exists = appointmentRepository.existsByDoctorAndAppointmentDateAndAppointmentTime(doctor, date, time);
        if (exists) {
            throw new IllegalStateException("Doctor already has an appointment at this time on " + date + " at " + time);
        }
        
        // Set initial status as SCHEDULED (pending doctor approval)
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .appointmentTime(time)
                .reason(reason.trim())
                .status(AppointmentStatus.SCHEDULED) // Changed from PENDING to SCHEDULED
                .createdAt(LocalDateTime.now())
                .build();
        
        return appointmentRepository.save(appointment);
    }
    
    public List<Appointment> getByPatientId(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));
        
        return appointmentRepository.findByPatient(patient);
    }
    
    public Appointment updateStatus(Long id, AppointmentStatus status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("Appointment ID and status are required");
        }
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + id));
        
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
    
    public List<Appointment> getByDoctorId(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor ID cannot be null");
        }
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));
        
        return appointmentRepository.findByDoctor(doctor);
    }
    
    // Admin can see all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    // Get appointments based on user role
    public List<Appointment> getAppointmentsByUserRole(String userEmail, String role) {
        switch (role.toUpperCase()) {
            case "ADMIN":
                return getAllAppointments();
            case "DOCTOR":
                return getMyDoctorAppointments(userEmail);
            case "PATIENT":
                // Get patient by email and return their appointments
                // You'll need to implement this based on your Patient model
                throw new RuntimeException("Patient appointment filtering not implemented yet");
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
    
    public Appointment getAppointmentById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Appointment ID cannot be null");
        }
        
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + id));
    }
    
    public void cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }
    
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Appointment not found with ID: " + id);
        }
        appointmentRepository.deleteById(id);
    }
    
    public List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
    }
}