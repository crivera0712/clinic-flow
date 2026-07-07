package com.clinicflow.clinic_flow.user_clinic_memberships;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserClinicMembershipId implements Serializable {
    private Long user;
    private Long clinic;
}
