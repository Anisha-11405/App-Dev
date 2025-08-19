package com.examly.springapp.controller;

import com.examly.springapp.model.*;
import com.examly.springapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        patient = patientRepository.save(Patient.builder().name("John Doe").email("john.doe@example.com").phoneNumber("1234567890").dateOfBirth(LocalDate.of(1990,1,1)).build());
        doctor = doctorRepository.save(Doctor.builder().name("Dr. Smith").specialization("Cardiology").email("smith@hospital.com").phoneNumber("9876543210").build());
    }

    @Test
    void testBookAppointment() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("patientId", patient.getId());
        req.put("doctorId", doctor.getId());
        req.put("appointmentDate", LocalDate.now().plusDays(1).toString());
        req.put("appointmentTime", "14:30:00");
        req.put("reason", "Annual checkup and blood pressure monitoring");
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void testBookAppointmentDoctorConflict() throws Exception {
        // Book once
        Appointment a = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(14, 0))
                .reason("Blood pressure checkup again")
                .status(AppointmentStatus.SCHEDULED)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        appointmentRepository.save(a);
        // Attempt conflict
        Map<String, Object> req2 = new HashMap<>();
        req2.put("patientId", patient.getId());
        req2.put("doctorId", doctor.getId());
        req2.put("appointmentDate", LocalDate.now().plusDays(1).toString());
        req2.put("appointmentTime", "14:00:00");
        req2.put("reason", "Valid reason.");
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateAppointmentStatus() throws Exception {
        Appointment appt = appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(LocalDate.now().plusDays(2))
                .appointmentTime(LocalTime.of(10, 0))
                .reason("Test status update")
                .status(AppointmentStatus.SCHEDULED)
                .createdAt(java.time.LocalDateTime.now())
                .build());
        Map<String, Object> st = new HashMap<>();
        st.put("status", "APPROVED");
        mockMvc.perform(patch("/api/appointments/"+appt.getId()+"/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(st)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testGetAppointmentsByPatient() throws Exception {
        Appointment appt = appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(LocalDate.now().plusDays(3))
                .appointmentTime(LocalTime.of(15, 0))
                .reason("See all appts")
                .status(AppointmentStatus.SCHEDULED)
                .createdAt(java.time.LocalDateTime.now())
                .build());
        mockMvc.perform(get("/api/appointments/patient/"+patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }
    
    @Test
    void testGetAppointmentsByPatientNotFound() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/99999"))
                .andExpect(status().isNotFound());
    }
}
