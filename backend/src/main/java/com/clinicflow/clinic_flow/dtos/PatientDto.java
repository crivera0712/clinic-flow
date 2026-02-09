package com.clinicflow.clinic_flow.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PatientDto {
    private Long id;
    private String firstName;
    private String lastName;
}
