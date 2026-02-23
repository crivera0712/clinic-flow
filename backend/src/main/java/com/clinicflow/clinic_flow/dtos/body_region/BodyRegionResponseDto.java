package com.clinicflow.clinic_flow.dtos.body_region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BodyRegionResponseDto {
    private Long id;
    private String code;
    private String displayName;
    private Boolean isActive;
}
