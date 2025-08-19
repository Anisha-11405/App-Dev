package com.examly.springapp.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.examly.springapp.model.Appointment;
import com.examly.springapp.model.AppointmentStatus;

public record AppointmentDto(
        Long id,
        Long patientId,
        Long doctorId,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        AppointmentStatus status
) {
    public static AppointmentDto from(Appointment a) {
        return new AppointmentDto(
                a.getId(),
                a.getPatient().getId(),
                a.getDoctor().getId(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getStatus()
        );
    }
}
