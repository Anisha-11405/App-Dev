package com.examly.springapp.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Patient;
import com.examly.springapp.model.Role;
import com.examly.springapp.model.Admin;
import com.examly.springapp.repository.DoctorRepository;
import com.examly.springapp.repository.PatientRepository;
import com.examly.springapp.repository.AdminRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          AdminRepository adminRepository,
                          PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Initialize Patients
        if (patientRepository.count() == 0) {
            Patient p1 = new Patient();
            p1.setName("John Doe");
            p1.setEmail("john@example.com");
            p1.setPhoneNumber("9876543210");
            p1.setDateOfBirth(LocalDate.of(1990, 5, 20));
            p1.setPassword(passwordEncoder.encode("password123"));
            p1.setRole(Role.PATIENT);
            patientRepository.save(p1);

            Patient p2 = new Patient();
            p2.setName("Alice Johnson");
            p2.setEmail("alice@example.com");
            p2.setPhoneNumber("9123456780");
            p2.setDateOfBirth(LocalDate.of(1985, 3, 15));
            p2.setPassword(passwordEncoder.encode("password456"));
            p2.setRole(Role.PATIENT);
            patientRepository.save(p2);
            
            System.out.println("✅ Sample patients created");
        }

        // Initialize Doctors
        if (doctorRepository.count() == 0) {
            Doctor doctor1 = new Doctor();
            doctor1.setName("Dr. Smith");
            doctor1.setEmail("smith@example.com");
            doctor1.setPhoneNumber("1234567890");
            doctor1.setSpecialization("Cardiology");
            doctor1.setPassword(passwordEncoder.encode("doctor123"));
            doctor1.setRole(Role.DOCTOR);
            doctorRepository.save(doctor1);

            Doctor d2 = new Doctor();
            d2.setName("Dr. Emily");
            d2.setEmail("dremily@example.com");
            d2.setPhoneNumber("0987654321");
            d2.setSpecialization("Neurology");
            d2.setPassword(passwordEncoder.encode("doctor456"));
            d2.setRole(Role.DOCTOR);
            doctorRepository.save(d2);
            
            System.out.println("✅ Sample doctors created");
        }

        // Initialize Admin Users
        if (adminRepository.count() == 0) {
            Admin admin1 = new Admin();
            admin1.setName("System Admin");
            admin1.setEmail("admin@example.com");
            admin1.setPhoneNumber("1111111111");
            admin1.setPassword(passwordEncoder.encode("admin123"));
            admin1.setRole(Role.ADMIN);
            adminRepository.save(admin1);

            Admin admin2 = new Admin();
            admin2.setName("Hospital Administrator");
            admin2.setEmail("hospital.admin@example.com");
            admin2.setPhoneNumber("2222222222");
            admin2.setPassword(passwordEncoder.encode("hospital456"));
            admin2.setRole(Role.ADMIN);
            adminRepository.save(admin2);
            
            System.out.println("✅ Sample admin users created");
            System.out.println("📧 Admin login: admin@example.com / admin123");
            System.out.println("📧 Hospital Admin login: hospital.admin@example.com / hospital456");
        }
        
        System.out.println("🚀 Data initialization complete!");
        System.out.println("📊 Test accounts:");
        System.out.println("   Patient: john@example.com / password123");
        System.out.println("   Doctor: smith@example.com / doctor123");
        System.out.println("   Admin: admin@example.com / admin123");
    }
}