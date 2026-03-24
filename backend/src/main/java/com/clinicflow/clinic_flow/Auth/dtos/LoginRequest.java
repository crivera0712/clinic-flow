package com.clinicflow.clinic_flow.Auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "username required")
    private String username;

    @NotBlank(message = "password required")
    private String password;
}
