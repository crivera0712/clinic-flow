package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public Page<PatientResponseDto> getPatients(Pageable pageable) {
        var clinicId = currentUserService.getCurrentClinicId();
        return patientRepository.findAllByClinicId(clinicId, pageable).map(patientMapper::toPatientResponseDto);
    }

    public PatientResponseDto getPatient(Long id) {
        var clinicId = currentUserService.getCurrentClinicId();
        return patientMapper.toPatientResponseDto(findByIdAndClinicIdOrElse(id, clinicId));
    }

    public List<PatientResponseDto> searchPatient(String tokens) {
        var clinicId = currentUserService.getCurrentClinicId();

        if (tokens == null || tokens.trim().isEmpty()) {
            throw new IllegalArgumentException("search query must not be blank");
        }

        String[] tokenArray = tokens.trim().split("\\s+");
        if (tokenArray.length >= 2) {
            return patientRepository.searchPatientByTwoTokens(tokenArray[0], tokenArray[1], clinicId).stream()
                    .map(patientMapper::toPatientResponseDto)
                    .toList();
        }
        return patientRepository.searchPatientByOneToken(tokenArray[0], clinicId).stream()
                .map(patientMapper::toPatientResponseDto)
                .toList();
    }

    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto request) {
        var clinic = clinicContextService.requireWritableClinic();
        Patient patient = patientMapper.toPatient(request);
        patient.setClinic(clinic);
        return patientMapper.toPatientResponseDto(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponseDto updatePatient(Long id, PatientPatchDto request) {
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        var patient = findByIdAndClinicIdOrElse(id, clinicId);

        if (request.getFirstName() != null) patient.setFirstName(request.getFirstName());
        if (request.getLastName() != null) patient.setLastName(request.getLastName());

        return patientMapper.toPatientResponseDto(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long id) {
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        patientRepository.delete(findByIdAndClinicIdOrElse(id, clinicId));
    }

    private Patient findByIdAndClinicIdOrElse(Long id, Long clinicId) {
        return patientRepository.findByIdAndClinicId(id, clinicId).orElseThrow(() -> new PatientNotFoundException(id));
    }
}
