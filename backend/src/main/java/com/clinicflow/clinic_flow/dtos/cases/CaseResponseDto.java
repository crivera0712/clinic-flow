package com.clinicflow.clinic_flow.dtos.cases;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CaseResponseDto {
    private Long id;
    private Long patientId;
    private Long bodyRegionId;
}
