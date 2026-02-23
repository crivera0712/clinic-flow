package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.body_region.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.dtos.body_region.BodyRegionResponseDto;
import com.clinicflow.clinic_flow.services.BodyRegionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/bodyregion")
@RestController
public class BodyRegionController {
    private final BodyRegionService bodyRegionService;
    @GetMapping
    public List<BodyRegionResponseDto> getBodyRegions() {
        return bodyRegionService.getBodyRegions();
    }

    @GetMapping("/{id}")
    public BodyRegionResponseDto getBodyRegion(@PathVariable Long id) {
        return bodyRegionService.getBodyRegionById(id);
    }

    @PostMapping("/create")
    public BodyRegionResponseDto createBodyRegion(@RequestBody @Valid BodyRegionRequestDto request) {
        return bodyRegionService.createBodyRegion(request);
    }

}
