package com.clinicflow.clinic_flow.dtos.users;

import com.clinicflow.clinic_flow.entity.Users;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "username is required")
    @Size(max = 255, message = "username must be less than 255 characters")
    private String username;

    @NotBlank
    @Size(min = 6, message = "password is too short")
    private String passwordHash;
    private boolean enabled;
    private Users.RoleName role;
}
