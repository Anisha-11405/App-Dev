package com.examly.springapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.PatientDto;
import com.examly.springapp.model.Patient;
import com.examly.springapp.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    @Autowired
    private PatientService patientService;

    

    @PostMapping
    public ResponseEntity<Patient>createPatient(@Valid @RequestBody Patient patient){
        Patient saved=patientService.createPatient(patient);
        return new ResponseEntity<>(saved,HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Patient>>getAllPatients(){
        return ResponseEntity.ok(patientService.getAllPatients());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Patient>getPatientById(@PathVariable Long id){
        Patient patient=patientService.getPatientById(id);
        return patient!=null?ResponseEntity.ok(patient):ResponseEntity.notFound().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Patient>updatePatient(@PathVariable Long id, @RequestBody Patient patient){
        Patient updated=patientService.updatePatient(id, patient);
        return updated!=null?ResponseEntity.ok(updated):ResponseEntity.notFound().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deletePatient(@PathVariable Long id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Patient> createPatientAsAdmin(@RequestBody Patient patient) {
    return ResponseEntity.ok(patientService.createPatient(patient));
}

@GetMapping("/secure/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
public ResponseEntity<Patient> getPatientSecure(@PathVariable Long id) {
    Patient patient = patientService.getPatientById(id);
    return patient != null ? ResponseEntity.ok(patient) : ResponseEntity.notFound().build();
}



}
