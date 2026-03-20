package com.clinicflow.clinic_flow.therapist.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TherapistsResponseDto {
    public Long id;
    public String therapistName;
    public String type;
}
