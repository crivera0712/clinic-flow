
package com.clinicflow.clinic_flow.config;

import com.clinicflow.clinic_flow.auth.filters.JwtAuthenticationFilter;
import com.clinicflow.clinic_flow.users.Users;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(c -> c
                        .requestMatchers(HttpMethod.GET, "/api/appointments/date").hasAnyRole(
                                Users.RoleName.ADMIN.name(), Users.RoleName.DISPLAY.name())
                        .requestMatchers("/api/appointments/**").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers("/api/bodyregion/**").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers("/api/patients/**").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers("/api/cases/**").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers("/api/therapists/**").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole(Users.RoleName.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/*/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler((req, res, ex) -> {
                        res.setStatus(HttpStatus.FORBIDDEN.value());
                    });
                });
        return http.build();
    }

}
