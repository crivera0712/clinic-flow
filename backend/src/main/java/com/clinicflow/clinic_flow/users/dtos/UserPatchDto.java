package com.clinicflow.clinic_flow.users.dtos;

import com.clinicflow.clinic_flow.users.Users;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPatchDto {
    @NotNull(message = "Role cannot be null")
    private Users.RoleName roleName;
}
