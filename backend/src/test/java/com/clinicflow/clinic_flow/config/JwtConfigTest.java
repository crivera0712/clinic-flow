package com.clinicflow.clinic_flow.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtConfigTest {

    @Test
    void shouldNormalizeLaxCookieSameSite() {
        JwtConfig config = new JwtConfig();
        config.setCookieSecure(false);
        config.setCookieSameSite("lax");

        assertDoesNotThrow(config::validate);
        assertEquals("Lax", config.getCookieSameSite());
    }

    @Test
    void shouldAllowNoneWhenCookieIsSecure() {
        JwtConfig config = new JwtConfig();
        config.setCookieSecure(true);
        config.setCookieSameSite("None");

        assertDoesNotThrow(config::validate);
        assertEquals("None", config.getCookieSameSite());
    }

    @Test
    void shouldRejectNoneWhenCookieIsNotSecure() {
        JwtConfig config = new JwtConfig();
        config.setCookieSecure(false);
        config.setCookieSameSite("None");

        assertThrows(IllegalStateException.class, config::validate);
    }

    @Test
    void shouldRejectUnsupportedCookieSameSiteValue() {
        JwtConfig config = new JwtConfig();
        config.setCookieSecure(true);
        config.setCookieSameSite("Strict");

        assertThrows(IllegalStateException.class, config::validate);
    }
}
