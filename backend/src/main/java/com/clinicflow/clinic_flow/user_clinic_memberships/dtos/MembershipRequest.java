package com.clinicflow.clinic_flow.user_clinic_memberships.dtos;

import lombok.Getter;

@Getter
public class MembershipRequest {
    private Long user;
    private Long clinic;
}
