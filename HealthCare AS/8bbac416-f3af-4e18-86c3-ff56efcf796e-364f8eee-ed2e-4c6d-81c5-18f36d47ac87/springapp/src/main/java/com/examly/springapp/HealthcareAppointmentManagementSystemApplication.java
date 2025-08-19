package com.examly.springapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
// public class HealthcareAppointmentManagementSystemApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(HealthcareAppointmentManagementSystemApplication.class, args);
// 	}

// }
@SpringBootApplication
public class HealthcareAppointmentManagementSystemApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting Healthcare App...");
        SpringApplication.run(HealthcareAppointmentManagementSystemApplication.class, args);
        System.out.println("✅ Healthcare App Started Successfully!");
    }
}
