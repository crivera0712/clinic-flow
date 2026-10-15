package com.clinicflow.clinic_flow.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinicflow.clinic_flow.clinics.Clinics;
import org.junit.jupiter.api.Test;

class UsersMapperTest {

    private final UsersMapper usersMapper = new UsersMapperImpl();

    @Test
    void shouldIncludeClinicDetails_whenMappingLoginResponse() {
        Clinics clinic = new Clinics();
        clinic.setName("Raccoon City Physical Therapy");
        clinic.setIsDemo(true);
        Users user = new Users();
        user.setId(7L);
        user.setUsername("demo_display");
        user.setRoleName(Users.RoleName.DISPLAY);
        user.setClinic(clinic);

        var response = usersMapper.toLoginResponse(user);

        assertEquals("demo_display", response.getUsername());
        assertEquals(Users.RoleName.DISPLAY, response.getRoleName());
        assertTrue(response.getIsDemo());
        assertEquals("Raccoon City Physical Therapy", response.getClinicName());
    }
}
