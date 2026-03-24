package com.clinicflow.clinic_flow.exception;

public class BodyRegionNotFoundException extends RuntimeException {
    public BodyRegionNotFoundException(Long id) {
        super("Could not find body region with id " + id);
    }
}
