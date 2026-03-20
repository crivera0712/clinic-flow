package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public List<PatientResponseDto> getPatients() {
        return patientRepository.findAll()
                .stream().map(patientMapper::toPatientResponseDto)
                .toList();
    }

    public PatientResponseDto getPatient(Long id) {
        return patientMapper.toPatientResponseDto(patientRepository.getPatientById((id)));
    }

    @Transactional
    public List<PatientResponseDto> searchPatient(String tokens) {
        String[] tokenArray = tokens.trim().split("\\s+");
        if (tokenArray.length == 1){
            return patientRepository.searchPatientByOneToken(tokenArray[0]);
        } else if (tokenArray.length == 2) {
            return patientRepository.searchPatientByTwoTokens(tokenArray[0], tokenArray[1]);
        }
        return patientRepository.searchPatientByOneToken(tokenArray[0]);
    }

    public PatientResponseDto createPatient (PatientRequestDto patientRequestDto) {
        Patient patient = patientMapper.toPatient(patientRequestDto);
        var newPatient = patientRepository.save(patient);
        return patientMapper.toPatientResponseDto(newPatient);
    }

    public PatientResponseDto updatePatient (Long id, PatientPatchDto request) {
        var patient = patientRepository.findById(id).orElseThrow( () ->
                new RuntimeException("Patient with id " + id + " does not exist"));

        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }

        return patientMapper.toPatientResponseDto(patientRepository.save(patient));
    }
}
