package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.cases.CaseRequestDto;
import com.clinicflow.clinic_flow.dtos.cases.CaseResponseDto;
import com.clinicflow.clinic_flow.services.CaseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/case")
public class CaseController {

    private final CaseService caseService;

    @GetMapping()
    public List<CaseResponseDto> getBodyRegions() {
        return caseService.getCases();
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
