package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
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

    public Page<PatientResponseDto> getPatients(Pageable pageable) {
        return patientRepository
                .findAll(pageable).map(patientMapper::toPatientResponseDto);
    }

    public PatientResponseDto getPatient(Long id) {
        Patient patient = patientRepository.getPatientById(id);
        if (patient == null) {
            throw new PatientNotFoundException(id);
        }
        return patientMapper.toPatientResponseDto(patient);
    }

    @Transactional
    public List<PatientResponseDto> searchPatient(String tokens) {
        if (tokens == null || tokens.trim().isEmpty()) {
            throw new IllegalArgumentException("search query must not be blank");
        }

        String[] tokenArray = tokens.trim().split("\\s+");
        if (tokenArray.length == 1){
            return patientRepository.searchPatientByOneToken(tokenArray[0])
                    .stream()
                    .map(patientMapper::toPatientResponseDto)
                    .toList();
        } else if (tokenArray.length == 2) {
            return patientRepository.searchPatientByTwoTokens(tokenArray[0], tokenArray[1])
                    .stream()
                    .map(patientMapper::toPatientResponseDto)
                    .toList();
        }
        return patientRepository.searchPatientByOneToken(tokenArray[0])
                .stream()
                .map(patientMapper::toPatientResponseDto)
                .toList();
    }

    @Transactional
    public PatientResponseDto createPatient (PatientRequestDto patientRequestDto) {
        Patient patient = patientMapper.toPatient(patientRequestDto);
        String dpn = (patient.getLastName() + ", " + patient.getFirstName().charAt(0));
        patient.setDisplayName(dpn);
        var newPatient = patientRepository.save(patient);
        return patientMapper.toPatientResponseDto(newPatient);
    }

    @Transactional
    public PatientResponseDto updatePatient (Long id, PatientPatchDto request) {
        var patient = patientRepository.findById(id).orElseThrow( () ->
                new PatientNotFoundException(id));

        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }

        if (request.getFirstName() != null || request.getLastName() != null) {
            patient.setDisplayName(updateDisplayName(patient.getFirstName(), patient.getLastName()));
        }

        return patientMapper.toPatientResponseDto(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient (Long id) {
        var patient = patientRepository.findById(id).orElseThrow( () ->
                new PatientNotFoundException(id));

        if (patient != null){
            patientRepository.deleteById(id);
        }
    }

    private String updateDisplayName (String firstName, String lastName) {
        return lastName + ", " + firstName.charAt(0);
    }

}
