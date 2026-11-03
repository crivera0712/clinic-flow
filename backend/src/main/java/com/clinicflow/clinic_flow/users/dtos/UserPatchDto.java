package com.clinicflow.clinic_flow.users.dtos;

import com.clinicflow.clinic_flow.users.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Both fields are optional; a field left out (or null) is left unchanged.
@Getter
@Setter
@NoArgsConstructor
public class UserPatchDto {
    private Users.RoleName roleName;

    private Boolean enabled;

    @JsonIgnore
    @AssertTrue(message = "provide roleName, enabled, or both")
    public boolean isAnyFieldPresent() {
        return roleName != null || enabled != null;
    }
}
