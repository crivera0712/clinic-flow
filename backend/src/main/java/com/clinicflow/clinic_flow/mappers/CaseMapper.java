package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.cases.CaseRequestDto;
import com.clinicflow.clinic_flow.dtos.cases.CaseResponseDto;
import com.clinicflow.clinic_flow.entity.Case;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CaseMapper {

    Case toCase(CaseRequestDto caseRequestDto);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "bodyRegionId", source = "bodyRegion.id")
    CaseResponseDto toCaseResponseDto(Case caseEntity);

}
