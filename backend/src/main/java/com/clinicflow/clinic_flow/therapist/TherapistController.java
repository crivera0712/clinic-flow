package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.common.PageResponse;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistPatchDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@AllArgsConstructor
@RequestMapping("/api/therapists")
@RestController()
public class TherapistController {
    private final TherapistService therapistService;

    @GetMapping()
    public ResponseEntity<PageResponse<TherapistsResponseDto>> getTherapists(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TherapistsResponseDto> result = therapistService.getTherapists(search, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TherapistsResponseDto> getTherapist(@PathVariable Long id) {
        return ResponseEntity.ok(therapistService.getTherapistById(id));
    }

    @PostMapping()
    public ResponseEntity<TherapistsResponseDto> createTherapist(
            @RequestBody @Valid TherapistRequestDto therapistRequestDto) {
        TherapistsResponseDto response = therapistService.createTherapist(therapistRequestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TherapistsResponseDto> updateTherapist(
            @PathVariable Long id, @RequestBody TherapistPatchDto patch) {

        var patchedTherapist = therapistService.updateTherapist(id, patch);

        return ResponseEntity.ok(patchedTherapist);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTherapist(@PathVariable Long id) {
        therapistService.deleteTherapist(id);
        return ResponseEntity.noContent().build();
    }


}
