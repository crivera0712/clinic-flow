package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.auth.records.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements  CurrentUserService {
    @Override
    public Long getCurrentClinicId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return principal.clinicId();
    }
}
