package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BodyRegionMapper {
    BodyRegion toBodyRegion(BodyRegionRequestDto bodyRegionRequestDto);
    BodyRegionResponseDto toBodyRegionResponseDto(BodyRegion bodyRegion);
}
