package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.therapist.dtos.TherapistPatchDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@RequestMapping("/api/therapist")
@RestController()
public class TherapistController {
    private final TherapistService therapistService;

    @GetMapping()
    public List<TherapistsResponseDto> getTherapists() {
        return therapistService.getTherapists();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TherapistsResponseDto> getTherapist(@PathVariable Long id) {
        return ResponseEntity.ok(therapistService.getTherapistById(id));
    }

    @PostMapping("/create")
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
    public void deleteTherapist(@PathVariable Long id) {
        therapistService.deleteTherapist(id);
    }


}
