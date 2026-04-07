package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionPatchDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.BodyRegionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class BodyRegionService {
    private final BodyRegionRepository bodyRegionRepository;
    private final BodyRegionMapper bodyRegionMapper;
    private final ClinicContextService clinicContextService;


    public Page<BodyRegionResponseDto> getBodyRegions(String search, Pageable pageable){
        Page<BodyRegion> result;
        if (search == null || search.isBlank()) {
            result = bodyRegionRepository.findAll(pageable);
        } else {
            String term = search.trim();
            result = bodyRegionRepository.findByCodeContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                    term,
                    term,
                    pageable
            );
        }
        return result.map(bodyRegionMapper::toBodyRegionResponseDto);
    }

    public BodyRegionResponseDto getBodyRegionById(Long id){
        return bodyRegionMapper.toBodyRegionResponseDto(bodyRegionRepository.findById(id).orElseThrow( () ->
                new BodyRegionNotFoundException(id)));
    }

    @Transactional
    public BodyRegionResponseDto createBodyRegion(BodyRegionRequestDto bodyRegionRequestDto){
        clinicContextService.assertWritableClinic();
        BodyRegion bodyRegion = bodyRegionMapper.toBodyRegion(bodyRegionRequestDto);
        var result = bodyRegionRepository.save(bodyRegion);
        return bodyRegionMapper.toBodyRegionResponseDto(result);
    }

    @Transactional
    public BodyRegionResponseDto updateBodyRegion(Long id, BodyRegionPatchDto patch){
        clinicContextService.assertWritableClinic();
        var patchBr = bodyRegionRepository.findById(id).orElseThrow( () ->
                new BodyRegionNotFoundException(id));

        if (patch.getCode() != null && !patch.getCode().isEmpty()) {
            patchBr.setCode(patch.getCode());
        }

        if (patch.getDisplayName() != null && !patch.getDisplayName().isEmpty()) {
            patchBr.setDisplayName(patch.getDisplayName());
        }

        if (patch.getIsActive() != null) {
            patchBr.setIsActive(patch.getIsActive());
        }
        return bodyRegionMapper.toBodyRegionResponseDto(bodyRegionRepository.save(patchBr));
    }

    @Transactional
    public void deleteBodyRegion(Long id) {
        clinicContextService.assertWritableClinic();
        var bodyRegion = bodyRegionRepository.findById(id).orElseThrow(() ->
                new BodyRegionNotFoundException(id));
        bodyRegionRepository.delete(bodyRegion);
    }
}
