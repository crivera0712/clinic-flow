package com.clinicflow.clinic_flow.dtos.therapists;

import com.clinicflow.clinic_flow.entity.Therapist;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TherapistRequestDto {
    @NotNull
    public String therapistName;
    @NotNull
    public Therapist.TherapistType type;
}
