package com.clinicflow.clinic_flow.dtos.patients;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PatientRequestDto {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
}