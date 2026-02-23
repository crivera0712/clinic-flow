package com.clinicflow.clinic_flow.dtos.therapists;

import com.clinicflow.clinic_flow.entity.Therapist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TherapistRequestDto {
    public String therapistName;
    public Therapist.TherapistType type;
}
