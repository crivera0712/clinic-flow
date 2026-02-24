package com.clinicflow.clinic_flow.dtos.cases;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
public class CaseRequestDto {
    @NotNull
    private Long patientId;
    @NotNull
    private Long bodyRegionId;
    private Instant createdAt;
}
