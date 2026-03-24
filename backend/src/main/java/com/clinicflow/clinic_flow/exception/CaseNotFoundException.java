package com.clinicflow.clinic_flow.exception;

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(Long id) {
        super("Could not find case by id " + id);
    }
}
