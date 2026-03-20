package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/case")
public class CaseController {

    private final CaseService caseService;

    @GetMapping()
    public List<CaseResponseDto> getCases() {
        return caseService.getCases();
    }

    @GetMapping("/patient/{id}")
    public List<CaseResponseDto> getCasesByPatient(@PathVariable Long id) {
        return caseService.searchByPatient(id);
    }

    @GetMapping("/{id}")
    public CaseResponseDto getCaseById(@PathVariable Long id) {
        return caseService.getCase(id);
    }

    @PostMapping("/create")
    public CaseResponseDto createCase(@RequestBody CaseRequestDto request) {
        return caseService.createCase(request);
    }
}
