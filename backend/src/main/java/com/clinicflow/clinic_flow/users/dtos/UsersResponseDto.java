package com.clinicflow.clinic_flow.users.dtos;

import com.clinicflow.clinic_flow.users.Users;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersResponseDto {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private Users.RoleName roleName;
}
