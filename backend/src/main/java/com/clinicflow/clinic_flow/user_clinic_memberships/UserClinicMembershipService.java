package com.clinicflow.clinic_flow.user_clinic_memberships;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.ClinicNotFoundException;
import com.clinicflow.clinic_flow.users.Users;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserClinicMembershipService {

    private final UserClinicMembershipRepository membershipRepository;

    public List<Clinics> getClinicsForUser(Long userId) {
        return membershipRepository.findAllByUser_Id(userId).stream()
                .map(UserClinicMembership::getClinic)
                .toList();
    }

    public Clinics requireClinicMembership(Long userId, String clinicSlug) {
        return membershipRepository
                .findByUser_IdAndClinic_Slug(userId, clinicSlug)
                .map(UserClinicMembership::getClinic)
                .orElseThrow(() -> new ClinicNotFoundException("Clinic not available for user"));
    }

    @Transactional
    public void createMembership(Users user, Clinics clinic) {
        UserClinicMembership membership = new UserClinicMembership();
        membership.setUser(user);
        membership.setClinic(clinic);
        membership.setCreatedAt(LocalDateTime.now());
        membershipRepository.save(membership);
    }
}
