package com.clinicflow.clinic_flow.therapist.dtos;


import com.clinicflow.clinic_flow.therapist.Therapist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TherapistPatchDto {
    private String therapistName;
    private Therapist.TherapistType therapistType;
}
