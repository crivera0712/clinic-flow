package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.PatientDto;
import com.clinicflow.clinic_flow.entity.Patient;
import com.clinicflow.clinic_flow.mappers.PatientMapper;
import com.clinicflow.clinic_flow.repositories.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
    can i build an api that returns all the patients within a date?
 */

@AllArgsConstructor
@RequestMapping("/patients")
@RestController
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @GetMapping
    public List<PatientDto> getPatients() {
        return patientRepository.findAll()
                .stream()
                .map(patientMapper::toPatientDto)
                .toList();
    }
}
