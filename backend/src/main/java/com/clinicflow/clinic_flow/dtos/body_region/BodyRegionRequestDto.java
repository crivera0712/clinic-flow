package com.clinicflow.clinic_flow.dtos.body_region;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BodyRegionRequestDto {
    @NotNull
    private String code;
    @NotNull
    private String displayName;
}
