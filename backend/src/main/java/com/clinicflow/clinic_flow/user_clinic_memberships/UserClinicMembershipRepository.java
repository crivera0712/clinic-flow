package com.clinicflow.clinic_flow.user_clinic_memberships;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserClinicMembershipRepository extends JpaRepository<UserClinicMembership, UserClinicMembershipId> {
    List<UserClinicMembership> findAllByUser_Id(Long userId);

    Optional<UserClinicMembership> findByUser_IdAndClinic_Id(Long userId, Long clinicId);

    Optional<UserClinicMembership> findByUser_IdAndClinic_Slug(Long userId, String clinicSlug);
}
