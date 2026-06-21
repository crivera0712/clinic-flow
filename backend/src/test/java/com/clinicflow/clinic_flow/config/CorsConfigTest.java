package com.clinicflow.clinic_flow.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CorsConfig.class)
            .withPropertyValues(
                    "app.cors.allowed-origins[0]=http://localhost:5173",
                    "app.cors.allowed-origins[1]=http://127.0.0.1:5173"
            );

    @Test
    void allowsEveryConfiguredDevelopmentOrigin() {
        contextRunner.run(context -> {
            var source = context.getBean(CorsConfigurationSource.class);
            var request = new MockHttpServletRequest("POST", "/api/auth/login");
            var configuration = source.getCorsConfiguration(request);

            assertThat(configuration).isNotNull();
            assertThat(configuration.checkOrigin("http://localhost:5173"))
                    .isEqualTo("http://localhost:5173");
            assertThat(configuration.checkOrigin("http://127.0.0.1:5173"))
                    .isEqualTo("http://127.0.0.1:5173");
            assertThat(configuration.checkOrigin("http://untrusted.example"))
                    .isNull();
        });
    }
}
