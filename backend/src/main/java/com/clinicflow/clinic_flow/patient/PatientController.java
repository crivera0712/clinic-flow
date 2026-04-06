package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.common.PageResponse;
import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;


@Validated
@AllArgsConstructor
@RequestMapping("/api/patients")
@RestController
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<PageResponse<PatientResponseDto>> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PatientResponseDto> result = patientService.getPatients(pageable);

        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public PatientResponseDto getPatientById(
            @PathVariable @Positive Long id){
        return patientService.getPatient(id);
    }

    @GetMapping("/search")
    public List<PatientResponseDto> searchPatient(
            @RequestParam String q){
        return patientService.searchPatient(q);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable @Positive Long id,
            @RequestBody @Valid PatientPatchDto patch){
        return ResponseEntity.ok(patientService.updatePatient(id, patch));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(@RequestBody @Valid PatientRequestDto request) {
        PatientResponseDto createdPatient = patientService.createPatient(request);

        URI location = URI.create("/api/patients/" + createdPatient.getId());


        return ResponseEntity.created(location).body(createdPatient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable @Positive Long id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

}
