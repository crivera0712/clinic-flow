package com.clinicflow.clinic_flow.user_clinic_memberships;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/membership")
@AllArgsConstructor
public class UserClinicMemberShipController {

    private final UserClinicMembershipService userClinicMembershipService;
}
