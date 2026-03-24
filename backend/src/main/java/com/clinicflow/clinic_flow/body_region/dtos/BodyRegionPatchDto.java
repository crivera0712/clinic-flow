package com.clinicflow.clinic_flow.body_region.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BodyRegionPatchDto {
    private String code;
    private Boolean isActive;
}
