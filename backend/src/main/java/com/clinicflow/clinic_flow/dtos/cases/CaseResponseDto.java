package com.clinicflow.clinic_flow.dtos.cases;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class CaseResponseDto {
    private Long id;
    private Long patientId;
    private Long bodyRegionId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;
}
