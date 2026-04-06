package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.cases.dtos.CasePatchDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    @GetMapping()
    public List<CaseResponseDto> getCases() {
        return caseService.getCases();
    }

    @GetMapping("/patient/{id}")
    public List<CaseResponseDto> getCasesByPatient(@PathVariable @NotNull Long id) {
        return caseService.searchByPatient(id);
    }

    @GetMapping("/{id}")
    public CaseResponseDto getCaseById(@PathVariable @NotNull Long id) {
        return caseService.getCase(id);
    }

    @PostMapping()
    public ResponseEntity<CaseResponseDto> createCase(@RequestBody @Valid CaseRequestDto request) {
        var response = caseService.createCase(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CaseResponseDto> updateCase(@PathVariable Long id, @RequestBody @Valid CasePatchDto patch) {

        var patchedCase = caseService.updateCase(id, patch);

        return ResponseEntity.ok(patchedCase);
    }


}
