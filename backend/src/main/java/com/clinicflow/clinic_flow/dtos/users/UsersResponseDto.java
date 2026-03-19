package com.clinicflow.clinic_flow.dtos.users;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsersResponseDto {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private String role;
}
