package com.examly.springapp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examly.springapp.model.Appointment;
import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Patient;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    boolean existsByDoctorAndAppointmentDateAndAppointmentTime(Doctor doctor, LocalDate date, LocalTime time);
    
    List<Appointment> findByPatient(Patient patient);
    
    List<Appointment> findByDoctor(Doctor doctor);
    
    List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Appointment> findByPatientAndStatus(Patient patient, com.examly.springapp.model.AppointmentStatus status);
    
    List<Appointment> findByDoctorAndStatus(Doctor doctor, com.examly.springapp.model.AppointmentStatus status);
    
    List<Appointment> findByStatus(com.examly.springapp.model.AppointmentStatus status);
    
    List<Appointment> findByDoctorAndAppointmentDateGreaterThanEqual(Doctor doctor, LocalDate date);
    
    List<Appointment> findByPatientAndAppointmentDateGreaterThanEqual(Patient patient, LocalDate date);
}