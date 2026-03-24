package com.clinicflow.clinic_flow.cases.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CasePatchDto {
    @NotNull(message = "Body region must not be missing")
    private Long bodyRegionId;
}
