package com.clinicflow.clinic_flow.auth.records;

import com.clinicflow.clinic_flow.auth.Jwt;

public record LoginResult(Jwt accessToken, Jwt refreshToken) {}
