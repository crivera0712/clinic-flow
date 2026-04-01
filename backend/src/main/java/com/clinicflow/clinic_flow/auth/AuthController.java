package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.AuthPrincipal;
import com.clinicflow.clinic_flow.auth.dtos.JwtResponse;
import com.clinicflow.clinic_flow.auth.dtos.LoginRequest;
import com.clinicflow.clinic_flow.auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.config.JwtConfig;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/logout")
    public ResponseEntity<Void>logout(HttpServletResponse response) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AuthPrincipal) auth.getPrincipal();
        var sid = principal.sid();

        authService.logout(sid);
        clearRefreshCookie(response);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ){
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired() || !"refresh".equals(jwt.getTokenType())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var result = authService.refreshToken(jwt);

        writeRefreshCookie(response, result.refreshToken().toString());

        return ResponseEntity.ok(new JwtResponse(result.accessToken().toString()));

    }

    @PostMapping("/validate")
    public boolean validate(@RequestHeader("Authorization") String authHeader){
        // **Todo check for revoked & expiry
        return authService.validateToken(authHeader);
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
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(jwtConfig.getRefreshTokenExpiration())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        var cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(jwtConfig.isCookieSecure())
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
