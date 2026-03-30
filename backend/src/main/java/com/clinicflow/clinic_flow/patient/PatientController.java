package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.common.PageResponse;
import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;


// **TODO add search by patientId in appointments service/controller using paging
// **TODO add admin specific endpoints?


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
            @PathVariable Long id){
        return patientService.getPatient(id);
    }

    @GetMapping("/search")
    public List<PatientResponseDto> searchPatient(
            @RequestParam String q){
        return patientService.searchPatient(q);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientPatchDto patch){
        return ResponseEntity.ok(patientService.updatePatient(id, patch));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(@RequestBody @Valid PatientRequestDto request) {
        PatientResponseDto createdPatient = patientService.createPatient(request);

        URI location = URI.create("/api/patients/" + createdPatient.getId());


        return ResponseEntity.created(location).body(createdPatient);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePatient(@RequestParam Long id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

}
