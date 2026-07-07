package com.clinicflow.clinic_flow.auth.dtos;

import com.clinicflow.clinic_flow.auth.Jwt;

public record LoginResult(Jwt accessToken, Jwt refreshToken) {}
