package com.clinicflow.clinic_flow.therapist.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TherapistRequestDto {
    @NotBlank(message = "therapist name must not be missing")
    @Size(max = 255, message = "name is too long")
    private String name;
}
