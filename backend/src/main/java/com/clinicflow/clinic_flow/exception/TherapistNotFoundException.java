package com.clinicflow.clinic_flow.exception;

public class TherapistNotFoundException extends RuntimeException {
    public TherapistNotFoundException(Long id) {
        super("Therapist with id " + id + " not found");
    }
}
