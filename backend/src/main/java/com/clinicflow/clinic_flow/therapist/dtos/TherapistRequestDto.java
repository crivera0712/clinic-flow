package com.clinicflow.clinic_flow.therapist.dtos;

import com.clinicflow.clinic_flow.therapist.Therapist;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TherapistRequestDto {
    @NotNull(message = "therapists' name must not be missing")

    @Size(max = 255, message = "name is too long")
    public String therapistName;

    @NotNull(message = "therapist type must not be missing")
    public Therapist.TherapistType type;
}
