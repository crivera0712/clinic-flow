package com.clinicflow.clinic_flow.patient.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PatientRequestDto {
    @NotBlank(message = "patient first name must not be missing")
    @Size(max = 255, message = "first name is too long!")
    private String firstName;

    @NotBlank(message = "patient last name must not be missing")
    @Size(max = 255, message = "last name is too long!")
    private String lastName;
}
