package com.clinicflow.clinic_flow.user_clinic_memberships;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.users.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_clinic_memberships")
@IdClass(UserClinicMembershipId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserClinicMembership {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Id
    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
