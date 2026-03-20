package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
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
