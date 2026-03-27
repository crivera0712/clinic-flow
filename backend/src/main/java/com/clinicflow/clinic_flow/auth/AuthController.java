package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.AuthPrincipal;
import com.clinicflow.clinic_flow.auth.dtos.JwtResponse;
import com.clinicflow.clinic_flow.auth.dtos.LoginRequest;
import com.clinicflow.clinic_flow.auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

        var cookie = new Cookie("refreshToken", result.refreshToken().toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(result.accessToken().toString()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void>logout(HttpServletResponse response) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AuthPrincipal) auth.getPrincipal();
        var sid = principal.sid();

        authService.logout(sid);

        var cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken") String refreshToken
    ){
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired() || !jwt.getTokenType().equals("refresh")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var result = authService.refreshToken(jwt);

        return ResponseEntity.ok(new JwtResponse(result.toString()));

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

}
