package com.serenity.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.serenity.shared.security.JwtAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serenity.shared.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper, GestionarAccesoUseCase acceso) {
        this.objectMapper = objectMapper;
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, objectMapper, acceso);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/google/vincular").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()   // registro y login sin token
                        .requestMatchers("/api/oyente/**").hasRole("OYENTE")
                        .requestMatchers("/api/moderacion/**").hasRole("MODERADOR")
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()          // para ver errores reales
                        .requestMatchers("/ws/**").permitAll()          // el JWT se valida en STOMP CONNECT
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> {
                    log.warn("Solicitud no autenticada rechazada");
                    escribir(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "Autenticación requerida");
                })
                .accessDeniedHandler((request, response, ex) -> {
                    log.warn("Solicitud sin autorización rechazada");
                    escribir(objectMapper, request, response, HttpStatus.FORBIDDEN, "Acceso denegado");
                }));
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private static void escribir(ObjectMapper mapper, jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response, HttpStatus status, String detail) throws java.io.IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), problem);
    }
}
