package com.clinicflow.clinic_flow.exception;

import java.time.LocalDateTime;

public class AppointmentAtTimeExistsException extends RuntimeException {
    public AppointmentAtTimeExistsException(LocalDateTime appointmentAtTime) {
        super("Appointment at the time " + appointmentAtTime + " already exists!");
    }
}
