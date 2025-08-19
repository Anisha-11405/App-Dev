package com.examly.springapp.controller;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.repository.DoctorRepository;
import com.examly.springapp.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void reset() {
        appointmentRepository.deleteAll(); // Remove all appointments before deleting doctors
        doctorRepository.deleteAll();
    }

    @Test
    void testCreateDoctor() throws Exception {
        Doctor doc = Doctor.builder()
                .name("Dr. Jane Smith")
                .specialization("Cardiology")
                .email("jane.smith@hospital.com")
                .phoneNumber("9876543210")
                .build();
        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.specialization").value("Cardiology"));
    }

    @Test
    void testGetAllDoctors() throws Exception {
        doctorRepository.saveAll(List.of(
                Doctor.builder().name("Ana").specialization("Surg").email("a@a.com").phoneNumber("1234567890").build(),
                Doctor.builder().name("Bob").specialization("GenMed").email("b@b.com").phoneNumber("0987654321").build()
        ));
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
    }
}
