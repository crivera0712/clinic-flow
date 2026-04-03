package com.clinicflow.clinic_flow.patient;

// **TODO implement entity graphs?

import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final CurrentUserService currentUserService;
    private final ClinicsRepository clinicsRepository;

    public Page<PatientResponseDto> getPatients(Pageable pageable) {
        var clinicId = currentUserService.getCurrentClinicId();

        Page<Patient> result;

        result = patientRepository.findAllByClinicId(clinicId, pageable);

        return result.map(patientMapper::toPatientResponseDto);
    }

    public PatientResponseDto getPatient(Long id) {
        var clinicId = currentUserService.getCurrentClinicId();

        var patient = findByIdAndClinicIdOrElse(id, clinicId);

        return patientMapper.toPatientResponseDto(patient);
    }

    public List<PatientResponseDto> searchPatient(String tokens) {
        var clinicId = currentUserService.getCurrentClinicId();

        if (tokens == null || tokens.trim().isEmpty()) {
            throw new IllegalArgumentException("search query must not be blank");
        }

        String[] tokenArray = tokens.trim().split("\\s+");
        if (tokenArray.length == 1){
            return patientRepository.searchPatientByOneToken(tokenArray[0], clinicId)
                    .stream()
                    .map(patientMapper::toPatientResponseDto)
                    .toList();
        } else if (tokenArray.length == 2) {
            return patientRepository.searchPatientByTwoTokens(tokenArray[0], tokenArray[1], clinicId)
                    .stream()
                    .map(patientMapper::toPatientResponseDto)
                    .toList();
        }
        return patientRepository.searchPatientByOneToken(tokenArray[0], clinicId)
                .stream()
                .map(patientMapper::toPatientResponseDto)
                .toList();
    }

    @Transactional
    public PatientResponseDto createPatient (PatientRequestDto request) {
        var clinicId = currentUserService.getCurrentClinicId();
        var clinic = clinicsRepository.getReferenceById(clinicId);

        Patient patient = patientMapper.toPatient(request);
        String dpn = buildDisplayName(request.getFirstName(), request.getLastName());
        patient.setDisplayName(dpn);
        patient.setClinic(clinic);

        var newPatient = patientRepository.save(patient);
        return patientMapper.toPatientResponseDto(newPatient);
    }

    @Transactional
    public PatientResponseDto updatePatient (Long id, PatientPatchDto request) {
        var  clinicId = currentUserService.getCurrentClinicId();
        var patient = findByIdAndClinicIdOrElse(id, clinicId);

        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }

        if (request.getFirstName() != null || request.getLastName() != null) {
            patient.setDisplayName(buildDisplayName(patient.getFirstName(), patient.getLastName()));
        }

        return patientMapper.toPatientResponseDto(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient (Long id) {
        var clinicId = currentUserService.getCurrentClinicId();
        var patient = findByIdAndClinicIdOrElse(id, clinicId);

        patientRepository.delete(patient);
    }

    // helper methods
    private String buildDisplayName (String firstName, String lastName) {
        return lastName + ", " + firstName.charAt(0);
    }

    private Patient findByIdAndClinicIdOrElse(Long id, Long clinicId) {
        return patientRepository.findByIdAndClinicId(id, clinicId).orElseThrow(() ->
                new PatientNotFoundException(id)
        );
    }

}
