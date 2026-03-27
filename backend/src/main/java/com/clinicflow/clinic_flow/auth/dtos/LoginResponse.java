package com.clinicflow.clinic_flow.auth.dtos;

import com.clinicflow.clinic_flow.users.Users;
import lombok.Data;

@Data
public class LoginResponse {
    private Long id;
    private String username;
    private Users.RoleName roleName;
}
