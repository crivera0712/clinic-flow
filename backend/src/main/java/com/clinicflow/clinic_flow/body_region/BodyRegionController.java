package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
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
