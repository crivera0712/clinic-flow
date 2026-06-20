package com.clinicflow.clinic_flow.auth.controllers;

import com.clinicflow.clinic_flow.auth.AuthService;
import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth.dtos.JwtResponse;
import com.clinicflow.clinic_flow.auth.dtos.LoginRequest;
import com.clinicflow.clinic_flow.auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.auth.records.AuthPrincipal;
import com.clinicflow.clinic_flow.users.dtos.CreateUserRequest;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.exception.InvalidSessionException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        var result = authService.login(request);
        writeRefreshCookie(response, result.refreshToken().toString());
        return ResponseEntity.ok(new JwtResponse(result.accessToken().toString()));
    }

    @PostMapping("/{clinicSlug}/register")
    public ResponseEntity<UsersResponseDto> register(
            @PathVariable @NotBlank String clinicSlug,
            @Valid @RequestBody CreateUserRequest request
    ) {
        var created = authService.register(clinicSlug, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void>logout(HttpServletResponse response) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AuthPrincipal) auth.getPrincipal();
        var sid = principal.sid();
        var clinicId = principal.clinicId();

        authService.logout(sid, clinicId);
        clearRefreshCookie(response);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ){
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("Refresh denied reason=missing_cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null) {
            log.info("Refresh denied reason=parse_failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (jwt.isExpired()) {
            log.info("Refresh denied reason=expired sid={} clinicId={}", jwt.getSid(), jwt.getClinicId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"refresh".equals(jwt.getTokenType())) {
            log.info(
                    "Refresh denied reason=wrong_token_type sid={} clinicId={} tokenType={}",
                    jwt.getSid(),
                    jwt.getClinicId(),
                    jwt.getTokenType()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            var result = authService.refreshToken(jwt);

            writeRefreshCookie(response, result.refreshToken().toString());

            return ResponseEntity.ok(new JwtResponse(result.accessToken().toString()));
        } catch (InvalidSessionException | BadCredentialsException ex) {
            log.info(
                    "Refresh denied reason=session_invalid sid={} clinicId={} message={}",
                    jwt.getSid(),
                    jwt.getClinicId(),
                    ex.getMessage()
            );
            throw ex;
        }

    }

    @PostMapping("/validate")
    public boolean validate(@RequestHeader("Authorization") String authHeader){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        return authService.validateToken(authHeader.substring(7));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me() {

        var response = authService.me();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        var cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(jwtConfig.isCookieSecure())
                .sameSite(jwtConfig.getCookieSameSite())
                .path("/api/auth/refresh")
                .maxAge(jwtConfig.getRefreshTokenExpiration())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        var cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(jwtConfig.isCookieSecure())
                .sameSite(jwtConfig.getCookieSameSite())
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
