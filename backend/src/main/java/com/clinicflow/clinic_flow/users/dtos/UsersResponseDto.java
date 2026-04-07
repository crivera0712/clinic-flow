package com.clinicflow.clinic_flow.users.dtos;

import com.clinicflow.clinic_flow.users.Users;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsersResponseDto {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private Users.RoleName roleName;
}
