package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/therapist")
@RestController()
public class TherapistController {
    private final TherapistService therapistService;

    @GetMapping()
    public List<TherapistsResponseDto> getTherapists() {
        return therapistService.getTherapists();
    }

    @GetMapping("/{id}")
    public TherapistsResponseDto getTherapist(@PathVariable Long id) {
        return therapistService.getTherapistById(id);
    }

    @PostMapping("/create")
    public TherapistsResponseDto createTherapist(@RequestBody TherapistRequestDto therapistRequestDto) {
        return therapistService.createTherapist(therapistRequestDto);
    }

}
