package com.examly.springapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.DoctorAvailability;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    
    List<DoctorAvailability> findByDoctorAndIsActive(Doctor doctor, Boolean isActive);
    
    List<DoctorAvailability> findByDoctor(Doctor doctor);
    
    Optional<DoctorAvailability> findByDoctorAndDayOfWeek(Doctor doctor, String dayOfWeek);
    
    @Modifying
    @Query("DELETE FROM DoctorAvailability da WHERE da.doctor = :doctor")
    void deleteByDoctor(@Param("doctor") Doctor doctor);
    
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.isActive = true")
    List<DoctorAvailability> findActiveAvailabilityByDoctorId(@Param("doctorId") Long doctorId);
    
    boolean existsByDoctorAndDayOfWeek(Doctor doctor, String dayOfWeek);
}