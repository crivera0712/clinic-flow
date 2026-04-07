package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionPatchDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionRequestDto;
import com.clinicflow.clinic_flow.body_region.dtos.BodyRegionResponseDto;
import com.clinicflow.clinic_flow.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@AllArgsConstructor
@RequestMapping("/api/bodyregion")
@RestController
public class BodyRegionController {
    private final BodyRegionService bodyRegionService;
    @GetMapping
    public ResponseEntity<PageResponse<BodyRegionResponseDto>> getBodyRegions(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BodyRegionResponseDto> result = bodyRegionService.getBodyRegions(search, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BodyRegionResponseDto> getBodyRegion(@PathVariable @NotNull Long id) {
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

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BodyRegionResponseDto> updateBodyRegion(@PathVariable Long id,
            @RequestBody @Valid BodyRegionPatchDto patch) {
        var patchedUser = bodyRegionService.updateBodyRegion(id, patch);
        return ResponseEntity.ok(patchedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBodyRegion(@PathVariable @NotNull Long id) {
        bodyRegionService.deleteBodyRegion(id);
        return ResponseEntity.noContent().build();
    }

}
