package com.clinicflow.clinic_flow.services;

import com.clinicflow.clinic_flow.dtos.cases.CaseRequestDto;
import com.clinicflow.clinic_flow.dtos.cases.CaseResponseDto;
import com.clinicflow.clinic_flow.entity.Case;
import com.clinicflow.clinic_flow.mappers.CaseMapper;
import com.clinicflow.clinic_flow.mappers.PatientMapper;
import com.clinicflow.clinic_flow.repositories.AppointmentRepository;
import com.clinicflow.clinic_flow.repositories.BodyRegionRepository;
import com.clinicflow.clinic_flow.repositories.CaseRepository;
import com.clinicflow.clinic_flow.repositories.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class CaseService {
    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;

    private final PatientRepository patientRepository;
    private final BodyRegionRepository bodyRegionRepository;

    public List<CaseResponseDto> getCases(){
        return caseRepository.findAll().stream().map(caseMapper::toCaseResponseDto).toList();
    }

    public CaseResponseDto getCase(Long id){
        return caseMapper.toCaseResponseDto(caseRepository.getCaseById(id));
    }

    public List<CaseResponseDto> searchByPatient(Long id){
        return caseRepository.getCaseByPatient(id).stream().map(caseMapper::toCaseResponseDto).toList();
    }

    public CaseResponseDto createCase(CaseRequestDto request){

        System.out.println("Case Request: " + request.toString());

        var patient = patientRepository.getPatientById(request.getPatientId());
        var bodyRegion = bodyRegionRepository.getBodyRegionById(request.getBodyRegionId());

        Case caseEntity = caseMapper.toCase(request);

        caseEntity.setPatient(patient);
        caseEntity.setBodyRegion(bodyRegion);
        caseEntity.setCreatedAt(Date.from(Instant.now()));

        var newCase = caseRepository.save(caseEntity);
        return caseMapper.toCaseResponseDto(newCase);
    }


}
