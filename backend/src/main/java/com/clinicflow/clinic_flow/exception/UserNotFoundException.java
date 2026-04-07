package com.clinicflow.clinic_flow.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super("User with id " + id + " not found");
    }

    public UserNotFoundException(String username) {
        super("User with name " + username + " not found");
    }
}
