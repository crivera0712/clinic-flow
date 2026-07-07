package com.clinicflow.clinic_flow.auth.filters;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth.records.AuthPrincipal;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthSessionService authSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        var token = authHeader.replace("Bearer ", "");
        var jwt = jwtService.parseToken(token);
        if (jwt == null) {
            log.debug("Invalid JWT received path={} method={}", request.getRequestURI(), request.getMethod());
            filterChain.doFilter(request, response);
            return;
        }

        if (jwt.isExpired()) {
            log.debug(
                    "Expired JWT received userId={} sessionId={} clinicId={} path={}",
                    jwt.getUserId(),
                    jwt.getSid(),
                    jwt.getClinicId(),
                    request.getRequestURI());
            authSessionService.checkRevokedAt(jwt.getSid(), jwt.getClinicId());
            filterChain.doFilter(request, response);
            return;
        }

        if ("access".equals(jwt.getTokenType())
                && authSessionService.isAccessSessionActive(jwt.getSid(), jwt.getClinicId())) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    new AuthPrincipal(jwt.getUserId(), jwt.getUsername(), jwt.getSid(), jwt.getClinicId()),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getRole())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug(
                    "Authenticated request userId={} username={} clinicId={} role={} path={}",
                    jwt.getUserId(),
                    jwt.getUsername(),
                    jwt.getClinicId(),
                    jwt.getRole(),
                    request.getRequestURI());
        } else {
            log.debug(
                    "Rejected JWT session userId={} sessionId={} clinicId={} tokenType={} path={}",
                    jwt.getUserId(),
                    jwt.getSid(),
                    jwt.getClinicId(),
                    jwt.getTokenType(),
                    request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
