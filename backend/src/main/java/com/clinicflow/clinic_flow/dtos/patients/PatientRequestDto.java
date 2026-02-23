package com.clinicflow.clinic_flow.dtos.patients;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PatientRequestDto {
    private String firstName;
    private String lastName;
}