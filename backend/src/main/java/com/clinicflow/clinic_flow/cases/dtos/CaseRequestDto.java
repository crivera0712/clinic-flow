package com.clinicflow.clinic_flow.cases.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
public class CaseRequestDto {
    @NotNull(message = "patient must not be missing")
    private Long patientId;

    @NotNull(message = "Body region must not be missing")
    private Long bodyRegionId;

    private Instant createdAt;
}
