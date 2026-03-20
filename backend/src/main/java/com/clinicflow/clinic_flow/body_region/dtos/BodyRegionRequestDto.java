package com.clinicflow.clinic_flow.body_region.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BodyRegionRequestDto {
    @NotBlank(message = "body region code must not be missing")
    private String code;

    @NotBlank(message = "display name must not be missing")
    private String displayName;
}
