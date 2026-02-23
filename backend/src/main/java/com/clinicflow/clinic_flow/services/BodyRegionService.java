package com.clinicflow.clinic_flow.services;

import com.clinicflow.clinic_flow.dtos.body_region.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.dtos.body_region.BodyRegionResponseDto;
import com.clinicflow.clinic_flow.entity.BodyRegion;
import com.clinicflow.clinic_flow.mappers.BodyRegionMapper;
import com.clinicflow.clinic_flow.repositories.BodyRegionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class BodyRegionService {
    private final BodyRegionRepository bodyRegionRepository;
    private final BodyRegionMapper bodyRegionMapper;


    public List<BodyRegionResponseDto> getBodyRegions(){
        return bodyRegionRepository
                .findAll()
                .stream()
                .map(bodyRegionMapper::toBodyRegionResponseDto)
                .toList();
    }

    public BodyRegionResponseDto getBodyRegionById(Long id){
        return bodyRegionMapper.toBodyRegionResponseDto(bodyRegionRepository.getBodyRegionById(id));
    }

    public BodyRegionResponseDto createBodyRegion(BodyRegionRequestDto bodyRegionRequestDto){
        BodyRegion bodyRegion = bodyRegionMapper.toBodyRegion(bodyRegionRequestDto);
        var result = bodyRegionRepository.save(bodyRegion);
        return bodyRegionMapper.toBodyRegionResponseDto(result);
    }


}
