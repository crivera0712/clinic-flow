package com.clinicflow.clinic_flow.exception;

public class DemoClinicReadOnlyException extends RuntimeException {
    public DemoClinicReadOnlyException() {
        super("Demo clinic is read-only");
    }
}
