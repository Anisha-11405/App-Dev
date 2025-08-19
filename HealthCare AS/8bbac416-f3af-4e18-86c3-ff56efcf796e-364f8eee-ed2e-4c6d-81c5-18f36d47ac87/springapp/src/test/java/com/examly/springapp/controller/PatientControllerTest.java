package com.examly.springapp.controller;

import com.examly.springapp.model.Patient;
import com.examly.springapp.repository.PatientRepository;
import com.examly.springapp.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll(); // Remove all appointments before deleting patients
        patientRepository.deleteAll();
    }
    
    @Test
    void testCreatePatient() throws Exception {
        Patient p = Patient.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .build();
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testCreatePatientValidation() throws Exception {
        Patient p = Patient.builder()
                .name("")
                .email("invalid")
                .phoneNumber("123")
                .dateOfBirth(LocalDate.of(2100, 1, 1))
                .build();
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", anyOf(
                    containsString("Name must be 3-50 characters"),
                    containsString("Name is required"),
                    containsString("Invalid email format"),
                    containsString("Phone number must be 10 digits"),
                    containsString("Date of birth must be in the past")
                )));
    }

    @Test
    void testGetAllPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
