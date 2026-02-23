package com.clinicflow.clinic_flow.dtos.cases;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CaseRequestDto {
    private Long patientId;
    private Long bodyRegionId;
}
