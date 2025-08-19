package com.examly.springapp.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "doctor_availability")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DoctorAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private Doctor doctor;
    
    @Column(nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "availability_time_slots",
        joinColumns = @JoinColumn(name = "availability_id")
    )
    @Column(name = "time_slot")
    private List<String> timeSlots;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
}