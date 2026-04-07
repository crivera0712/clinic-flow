package com.clinicflow.clinic_flow.auth.filters;

import com.clinicflow.clinic_flow.auth.Jwt;
import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth.records.AuthPrincipal;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.users.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final AuthSessionService authSessionService = mock(AuthSessionService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, authSessionService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWhenSessionIsActiveForClinic() throws Exception {
        Jwt jwt = mock(Jwt.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");

        when(jwtService.parseToken("access-token")).thenReturn(jwt);
        when(jwt.isExpired()).thenReturn(false);
        when(jwt.getTokenType()).thenReturn("access");
        when(jwt.getSid()).thenReturn("session-1");
        when(jwt.getClinicId()).thenReturn(7L);
        when(jwt.getUserId()).thenReturn(12L);
        when(jwt.getUsername()).thenReturn("sam");
        when(jwt.getRole()).thenReturn(Users.RoleName.ADMIN);
        when(authSessionService.isAccessSessionActive("session-1", 7L)).thenReturn(true);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        AuthPrincipal principal = (AuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(12L, principal.userId());
        assertEquals("sam", principal.username());
        assertEquals(7L, principal.clinicId());
    }

    @Test
    void shouldNotAuthenticateWhenSessionExistsInDifferentClinic() throws Exception {
        Jwt jwt = mock(Jwt.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");

        when(jwtService.parseToken("access-token")).thenReturn(jwt);
        when(jwt.isExpired()).thenReturn(false);
        when(jwt.getTokenType()).thenReturn("access");
        when(jwt.getSid()).thenReturn("session-1");
        when(jwt.getClinicId()).thenReturn(7L);
        when(authSessionService.isAccessSessionActive("session-1", 7L)).thenReturn(false);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(authSessionService).isAccessSessionActive("session-1", 7L);
    }
}
