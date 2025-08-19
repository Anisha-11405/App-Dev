package com.examly.springapp.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Patient;
import com.examly.springapp.repository.DoctorRepository;
import com.examly.springapp.repository.PatientRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (patientRepository.count() == 0) {
            Patient p1 = new Patient();
            p1.setName("John Doe");
            p1.setEmail("john@example.com");
            p1.setPhoneNumber("9876543210");
            p1.setDateOfBirth(LocalDate.of(1990, 5, 20));
            p1.setPassword(passwordEncoder.encode("password123"));
            patientRepository.save(p1);

            Patient p2 = new Patient();
            p2.setName("Alice Johnson");
            p2.setEmail("alice@example.com");
            p2.setPhoneNumber("9123456780");
            p2.setDateOfBirth(LocalDate.of(1985, 3, 15));
            p2.setPassword(passwordEncoder.encode("password456"));
            patientRepository.save(p2);
        }

        if (doctorRepository.count() == 0) {
            Doctor doctor1 = new Doctor();
            doctor1.setName("Dr. Smith");
            doctor1.setEmail("smith@example.com");
            doctor1.setPhoneNumber("1234567890");
            doctor1.setSpecialization("Cardiology");
            doctor1.setPassword(passwordEncoder.encode("doctor123"));
            doctorRepository.save(doctor1);

            Doctor d2 = new Doctor();
            d2.setName("Dr. Emily");
            d2.setEmail("dremily@example.com");
            d2.setPhoneNumber("0987654321");
            d2.setSpecialization("Neurology");
            d2.setPassword(passwordEncoder.encode("doctor456"));
            doctorRepository.save(d2);
        }
    }
}
