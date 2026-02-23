package com.clinicflow.clinic_flow.dtos.patients;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PatientResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
}
