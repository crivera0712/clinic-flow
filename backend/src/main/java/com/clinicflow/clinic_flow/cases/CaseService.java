package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.cases.dtos.CasePatchDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import com.clinicflow.clinic_flow.body_region.BodyRegionRepository;
import com.clinicflow.clinic_flow.exception.BodyRegionNotFoundException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import jakarta.transaction.Transactional;
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
        return caseMapper.toCaseResponseDto(caseRepository.findById(id).orElseThrow( () ->
                new CaseNotFoundException(id)));
    }

    public List<CaseResponseDto> searchByPatient(Long id){
        return caseRepository.getCaseByPatient(id).stream().map(caseMapper::toCaseResponseDto).toList();
    }

    @Transactional
    public CaseResponseDto createCase(CaseRequestDto request){
        var patient = patientRepository.getPatientById(request.getPatientId());
        if (patient == null) {
            throw new PatientNotFoundException(request.getPatientId());
        }

        BodyRegion bodyRegion = bodyRegionRepository.findById(request.getBodyRegionId()).orElseThrow( () ->
                new BodyRegionNotFoundException(request.getBodyRegionId()));

        Case caseEntity = caseMapper.toCase(request);

        caseEntity.setPatient(patient);
        caseEntity.setBodyRegion(bodyRegion);
        caseEntity.setCreatedAt(Date.from(Instant.now()));

        var newCase = caseRepository.save(caseEntity);
        return caseMapper.toCaseResponseDto(newCase);
    }

    @Transactional
    public CaseResponseDto updateCase(Long id, CasePatchDto patch){
        var caseEntity = caseRepository.findById(id).orElseThrow( () ->
                new CaseNotFoundException(id));

        if (patch != null && patch.getBodyRegionId() != null){
            var bodyRegion = bodyRegionRepository.findById(patch.getBodyRegionId()).orElseThrow( () ->
                    new BodyRegionNotFoundException(patch.getBodyRegionId()));

            caseEntity.setBodyRegion(bodyRegion);
        }

        return caseMapper.toCaseResponseDto(caseRepository.save(caseEntity));
    }

    @Transactional
    public void deleteCase(Long id){
        caseRepository.deleteById(id);
    }


}
