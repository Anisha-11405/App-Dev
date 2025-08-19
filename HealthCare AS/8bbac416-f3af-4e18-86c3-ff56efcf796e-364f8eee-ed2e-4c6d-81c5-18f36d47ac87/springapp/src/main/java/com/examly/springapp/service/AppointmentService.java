package com.examly.springapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .appointmentTime(time)
                .reason(reason.trim())
                .status(AppointmentStatus.SCHEDULED)
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
    
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
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