
package com.examly.springapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.examly.springapp.model.Doctor;
import com.examly.springapp.model.Doctor.ProfileStatus;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByEmail(String email);
    
    // New methods for FR2
    List<Doctor> findByStatus(ProfileStatus status);
    
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    
    List<Doctor> findByClinicNameContainingIgnoreCase(String clinicName);
    
    @Query("SELECT d FROM Doctor d WHERE d.userId = :userId")
    Optional<Doctor> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Doctor d WHERE d.specialization LIKE %:specialization% AND d.status = :status")
    List<Doctor> findBySpecializationAndStatus(@Param("specialization") String specialization, @Param("status") ProfileStatus status);
    
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.status = :status")
    long countByStatus(@Param("status") ProfileStatus status);
    
    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.specialization IS NOT NULL AND d.status = 'ACTIVE'")
    List<String> findActiveSpecializations();
    
    @Query("SELECT DISTINCT d.clinicName FROM Doctor d WHERE d.clinicName IS NOT NULL AND d.status = 'ACTIVE'")
    List<String> findActiveClinicNames();
}