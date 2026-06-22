package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@AllArgsConstructor
@RequestMapping("/api/therapists")
@RestController
public class TherapistController {
    private final TherapistService therapistService;

    @GetMapping()
    public ResponseEntity<List<TherapistsResponseDto>> getTherapists() {
        return ResponseEntity.ok(therapistService.getTherapists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TherapistsResponseDto> getTherapist(@PathVariable @Positive Long id) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTherapist(@PathVariable @Positive Long id) {
        therapistService.deleteTherapist(id);
        return ResponseEntity.noContent().build();
    }
}
