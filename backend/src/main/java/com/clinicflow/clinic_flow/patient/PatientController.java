package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/patients")
@RestController
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public List<PatientResponseDto> getPatients() {
        return patientService.getPatients();
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

    @PatchMapping("/update")
    public PatientResponseDto updatePatient(Long id, PatientPatchDto patch){
        return patientService.updatePatient(id, patch);
    }

    @PostMapping("/create")
    public ResponseEntity<PatientResponseDto> createPatient(@RequestBody @Valid PatientRequestDto request) {
        return ResponseEntity.ok(patientService.createPatient(request));
    }

}
