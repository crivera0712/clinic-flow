package com.clinicflow.clinic_flow.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("Username " + username + " already exists");
    }
}
