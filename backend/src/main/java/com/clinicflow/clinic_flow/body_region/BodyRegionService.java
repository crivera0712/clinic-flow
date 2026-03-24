package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionPatchDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
import com.clinicflow.clinic_flow.exception.BodyRegionNotFoundException;
import jakarta.transaction.Transactional;
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
        return bodyRegionMapper.toBodyRegionResponseDto(bodyRegionRepository.findById(id).orElseThrow( () ->
                new BodyRegionNotFoundException(id)));
    }

    @Transactional
    public BodyRegionResponseDto createBodyRegion(BodyRegionRequestDto bodyRegionRequestDto){
        BodyRegion bodyRegion = bodyRegionMapper.toBodyRegion(bodyRegionRequestDto);
        var result = bodyRegionRepository.save(bodyRegion);
        return bodyRegionMapper.toBodyRegionResponseDto(result);
    }

    @Transactional
    public BodyRegionResponseDto updateBodyRegion(Long id, BodyRegionPatchDto patch){
        var patchBr = bodyRegionRepository.findById(id).orElseThrow( () ->
                new BodyRegionNotFoundException(id));

        if (patch.getCode() != null && !patch.getCode().isEmpty()) {
            patchBr.setCode(patch.getCode());
        }

        if (patch.getIsActive() != null) {
            patchBr.setIsActive(patch.getIsActive());
        }
        return bodyRegionMapper.toBodyRegionResponseDto(bodyRegionRepository.save(patchBr));
    }
}
