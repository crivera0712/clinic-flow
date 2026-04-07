package com.clinicflow.clinic_flow.clinics;

import com.clinicflow.clinic_flow.exception.ClinicNotFoundException;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClinicContextService {

    private final ClinicsRepository clinicsRepository;
    private final CurrentUserService currentUserService;

    public Clinics getCurrentClinic() {
        Long clinicId = currentUserService.getCurrentClinicId();
        return clinicsRepository.findById(clinicId)
                .orElseThrow(() -> new ClinicNotFoundException("Could not find current clinic"));
    }

    public boolean isCurrentClinicDemo() {
        return Boolean.TRUE.equals(getCurrentClinic().getIsDemo());
    }

    public Clinics requireWritableClinic() {
        Clinics clinic = getCurrentClinic();
        if (Boolean.TRUE.equals(clinic.getIsDemo())) {
            throw new DemoClinicReadOnlyException();
        }
        return clinic;
    }

    public void assertWritableClinic() {
        if (isCurrentClinicDemo()) {
            throw new DemoClinicReadOnlyException();
        }
    }
}
