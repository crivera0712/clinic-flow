package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.body_region.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.dtos.body_region.BodyRegionResponseDto;
import com.clinicflow.clinic_flow.entity.BodyRegion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BodyRegionMapper {
    BodyRegion toBodyRegion(BodyRegionRequestDto bodyRegionRequestDto);
    BodyRegionResponseDto toBodyRegionResponseDto(BodyRegion bodyRegion);
}
