package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CaseMapper {

    Case toCase(CaseRequestDto caseRequestDto);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "bodyRegionId", source = "bodyRegion.id")
    CaseResponseDto toCaseResponseDto(Case caseEntity);

}
