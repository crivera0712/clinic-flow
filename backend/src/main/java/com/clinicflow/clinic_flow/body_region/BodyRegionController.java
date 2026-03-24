package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionPatchDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@RequestMapping("/api/bodyregion")
@RestController
public class BodyRegionController {
    private final BodyRegionService bodyRegionService;
    @GetMapping
    public ResponseEntity<List<BodyRegionResponseDto>> getBodyRegions() {
        return ResponseEntity.ok(bodyRegionService.getBodyRegions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BodyRegionResponseDto> getBodyRegion(@PathVariable Long id) {
        return ResponseEntity.ok(bodyRegionService.getBodyRegionById(id));
    }

    @PostMapping()
    public ResponseEntity<BodyRegionResponseDto> createBodyRegion(@RequestBody @Valid BodyRegionRequestDto request) {
        var response = bodyRegionService.createBodyRegion(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BodyRegionResponseDto> updateBodyRegion(@PathVariable Long id, @RequestBody @Valid BodyRegionPatchDto patch) {
        var patchedUser = bodyRegionService.updateBodyRegion(id, patch);
        return ResponseEntity.ok(patchedUser);
    }

}
