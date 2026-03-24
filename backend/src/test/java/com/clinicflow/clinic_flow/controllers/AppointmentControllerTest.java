package com.clinicflow.clinic_flow.controllers;
import com.clinicflow.clinic_flow.mappers.AppointmentMapper;
import com.clinicflow.clinic_flow.repositories.AppointmentRepository;
import com.clinicflow.clinic_flow.services.AppointmentService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    AppointmentRepository appointmentRepository;
    @MockitoBean
    AppointmentMapper  appointmentMapper;
    @MockitoBean
    AppointmentController appointmentController;
    @MockitoBean
    AppointmentService appointmentService;


    @Test
    void testPostRequestAppointment() throws Exception {
        var json = """
                {
                    "scheduledAt": "2026-02-13T09:00:00",
                    "therapistId": 1,
                    "caseId": 1
                }
                """;
        mockMvc.perform(post("/appointments/create")
                .contentType((MediaType.APPLICATION_JSON))
                .content(json))
                .andDo(print())
                .andExpect(status().isOk());

    }
}
