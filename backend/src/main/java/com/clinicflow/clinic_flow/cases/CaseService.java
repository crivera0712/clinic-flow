package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.cases.dtos.CasePatchDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import com.clinicflow.clinic_flow.body_region.BodyRegionRepository;
import com.clinicflow.clinic_flow.exception.BodyRegionNotFoundException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.users.CurrentUserService;
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
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public List<CaseResponseDto> getCases(){
        var clinicId = currentUserService.getCurrentClinicId();
        return caseRepository
                .findAllByClinicId(clinicId)
                .stream()
                .map(caseMapper::toCaseResponseDto)
                .toList();
    }

    public CaseResponseDto getCase(Long id){
        var clinicId = currentUserService.getCurrentClinicId();
        return caseMapper.toCaseResponseDto(findCaseOrThrow(id, clinicId));
    }

    public List<CaseResponseDto> searchByPatient(Long id){
        var clinicId = currentUserService.getCurrentClinicId();
        return caseRepository.getCaseByPatient(id, clinicId).stream().map(caseMapper::toCaseResponseDto).toList();
    }

    @Transactional
    public CaseResponseDto createCase(CaseRequestDto request){
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        var patient = patientRepository.findByIdAndClinicId(request.getPatientId(), clinicId).orElseThrow(() ->
                new PatientNotFoundException(request.getPatientId()));

        BodyRegion bodyRegion = bodyRegionRepository.findById(request.getBodyRegionId()).orElseThrow( () ->
                new BodyRegionNotFoundException(request.getBodyRegionId()));

        Case caseEntity = caseMapper.toCase(request);

        caseEntity.setPatient(patient);
        caseEntity.setBodyRegion(bodyRegion);
        caseEntity.setCreatedAt(Date.from(Instant.now()));
        caseEntity.setClinic(patient.getClinic());

        var newCase = caseRepository.save(caseEntity);
        return caseMapper.toCaseResponseDto(newCase);
    }

    @Transactional

    public CaseResponseDto updateCase(Long id, CasePatchDto patch){
        clinicContextService.assertWritableClinic();
        var caseEntity = findCaseOrThrow(id, currentUserService.getCurrentClinicId());

        if (patch != null && patch.getBodyRegionId() != null){
            var bodyRegion = bodyRegionRepository.findById(patch.getBodyRegionId()).orElseThrow( () ->
                    new BodyRegionNotFoundException(patch.getBodyRegionId()));

            caseEntity.setBodyRegion(bodyRegion);
        }

        return caseMapper.toCaseResponseDto(caseRepository.save(caseEntity));
    }

    @Transactional
    public void deleteCase(Long id){
        clinicContextService.assertWritableClinic();
        var caseEntity = findCaseOrThrow(id, currentUserService.getCurrentClinicId());
        caseRepository.delete(caseEntity);
    }

    private Case findCaseOrThrow(Long id,Long clinicId){
        return caseRepository.findByIdAndClinicId(id, clinicId).orElseThrow( () ->
                new CaseNotFoundException(id));
    }

}
