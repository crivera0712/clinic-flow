package com.clinicflow.clinic_flow.patient.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PatientResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String displayName;
}
