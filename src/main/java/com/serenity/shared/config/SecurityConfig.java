package com.serenity.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serenity.shared.security.JwtAuthenticationFilter;
import com.serenity.shared.security.JwtUtil;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log =
            LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtUtil jwtUtil,
            ObjectMapper objectMapper,
            GestionarAccesoUseCase acceso
    ) {
        this.objectMapper = objectMapper;
        this.jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtUtil, objectMapper, acceso);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                .cors(Customizer.withDefaults())

                // CSRF deshabilitado porque esta API REST utiliza autenticación
                // mediante JWT enviado explícitamente en la cabecera Authorization: Bearer.
                // Las solicitudes no dependen de cookies de autenticación ni de sesiones HTTP.
                // Si cambia el mecanismo de autenticación, se debe reevaluar esta decisión.
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Permitir solicitudes CORS preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        // Vinculación de Google: requiere autenticación
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/google/vincular"
                        ).authenticated()

                        // Rutas públicas de autenticación
                        .requestMatchers("/api/auth/**").permitAll()

                        // Acceso por roles
                        .requestMatchers("/api/oyente/**").hasRole("OYENTE")
                        .requestMatchers("/api/moderacion/**").hasRole("MODERADOR")

                        // Salud del servicio
                        .requestMatchers("/actuator/health").permitAll()

                        // Frase motivacional pública
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/frases/motivacional"
                        ).permitAll()

                        // Documentación OpenAPI
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Manejo de errores
                        .requestMatchers("/error").permitAll()

                        // Las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> {
                    log.warn("Solicitud no autenticada rechazada");

                    escribir(
                            objectMapper,
                            request,
                            response,
                            HttpStatus.UNAUTHORIZED,
                            "Autenticación requerida"
                    );
                })
                .accessDeniedHandler((request, response, ex) -> {
                    log.warn("Solicitud sin autorización rechazada");

                    escribir(
                            objectMapper,
                            request,
                            response,
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado"
                    );
                })
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escribir(
            ObjectMapper mapper,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String detail
    ) throws IOException {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        mapper.writeValue(response.getOutputStream(), problem);
    }
}
